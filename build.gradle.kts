import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

plugins {
    alias(libs.plugins.shadow)
    alias(libs.plugins.spotless)
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.3"
    java
}

group = "de.chojo"
version = "1.18.0"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://eldonexus.de/repository/maven-public")
    maven("https://eldonexus.de/repository/maven-proxies")
    maven("https://m2.dv8tion.net/releases")
}

spotless {
    java {
        licenseHeaderFile(rootProject.file("HEADER.txt"))
        target("**/*.java")
    }
}

dependencies {
    // discord
    implementation("de.chojo", "cjda-util", "2.12.0+jda-6.0.0") {
        exclude(group = "club.minnced", module = "opus-java")
    }

    val openapi = "6.7.0-5"

    annotationProcessor("io.javalin.community.openapi:openapi-annotation-processor:$openapi")
    implementation("io.javalin.community.openapi:javalin-openapi-plugin:$openapi") // for /openapi route with JSON scheme
    implementation("io.javalin.community.openapi:javalin-swagger-plugin:$openapi") // for Swagger UI

    // database
    implementation("org.postgresql", "postgresql", "42.7.8")
    implementation(libs.bundles.sadu)

    // Logging
    implementation(libs.bundles.log4j)
    implementation("de.chojo", "log-util", "1.0.1") {
        exclude("org.apache.logging.log4j")
    }

    implementation("org.knowm.xchart", "xchart", "3.8.8")

    // unit testing
    testImplementation(testlibs.bundles.junit)
    testImplementation("org.junit.platform", "junit-platform-launcher")
    testImplementation("org.knowm.xchart", "xchart", "3.8.8")
    testImplementation(testlibs.sadu.testing)
    testImplementation(testlibs.bundles.database.postgres)
    testImplementation(testlibs.slf4j.simple)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
    withJavadocJar()
}

idea {
    project {
        settings {
            runConfigurations {
                register<org.jetbrains.gradle.ext.Application>("App-Testing") {
                    mainClass = "de.chojo.repbot.ReputationBot"
                    jvmArgs = listOf("-Dbot.config=config/config.testing.json",
                            "-Dlog4j2.configurationFile=docker/config/log4j2.testing.xml",
                            "-Dcjda.localisation.error.name=false",
                            "-Dcjda.interactions.cleanguildcommands=true",
                            "-Dcjda.interactions.testmode=true").joinToString(" ")
                    moduleName = "rep-bot.main"
                }
            }
        }
    }
}

tasks {
    processResources {
        from(sourceSets.main.get().resources.srcDirs) {
            filesMatching("version") {
                var version = project.version.toString()
                var workflow = (System.getenv("GITHUB_ACTIONS") ?: "false") == "true"
                if (workflow) {
                    val now = ZonedDateTime.now(ZoneOffset.UTC)
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    val formattedDate = now.format(formatter)

                    version = when (System.getenv("GITHUB_REF_TYPE")) {
                        "branch" -> "$version ${System.getenv("GITHUB_REF_NAME")}-${System.getenv("GITHUB_SHA").substring(0, 7)} @ $formattedDate"
                        "tag" -> "$version ${System.getenv("GITHUB_REF_NAME").substring(1)} @ $formattedDate"
                        else -> "$version snapshot"
                    }
                }
                expand(
                        "version" to version
                )
            }
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }
    }

    compileJava {
        options.encoding = "UTF-8"
    }

    javadoc {
        options.encoding = "UTF-8"
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    shadowJar {
        transform(Log4j2PluginsCacheFileTransformer::class.java)
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "de.chojo.repbot.ReputationBot"))
        }
    }

    build {
        dependsOn(shadowJar)
    }
}
