import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

plugins {
    alias(libs.plugins.shadow)
    alias(libs.plugins.spotless)
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.4"
    java
}

group = "de.chojo"
version = "2.3.0"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://eldonexus.de/repository/maven-public")
    maven("https://eldonexus.de/repository/maven-proxies")
    maven("https://m2.dv8tion.net/releases")
}

spotless {
    java {
        target("src/**/*.java")
        licenseHeaderFile(rootProject.file("HEADER.txt"))
        trimTrailingWhitespace()
        endWithNewline()
        palantirJavaFormat("2.84.0")
                .formatJavadoc(false)
        removeUnusedImports()
        importOrder("", "java", "javax", "\\#")
        encoding("UTF-8")
    }

    format("javascript") {
        licenseHeaderFile(rootProject.file("HEADER.txt"), "(import|const|let|var|export|//)")
        target("frontend/src/**/*.js", "frontend/src/**/*.ts")
        targetExclude("frontend/node_modules/**", "frontend/dist/**")
        trimTrailingWhitespace()
        endWithNewline()
    }

    format("vue") {
        licenseHeaderFile(rootProject.file("HEADER.txt"), "(<template|<script|<style)")
        target("frontend/src/**/*.vue")
        targetExclude("frontend/node_modules/**", "frontend/dist/**")
        trimTrailingWhitespace()
        endWithNewline()
    }

    format("backendLocales") {
        encoding("UTF-8")
        target("src/main/resources/locale*.properties")
    }

    format("frontendLocales") {
        encoding("UTF-8")
        target("frontend/src/locales/*.json")
    }
}

dependencies {
    // discord
    implementation("de.chojo", "cjda-util", "2.13.1+jda-6.3.0") {
        exclude(group = "club.minnced", module = "opus-java")
    }

    val openapi = "6.7.0-5"

    annotationProcessor("io.javalin.community.openapi:openapi-annotation-processor:$openapi")
    implementation("io.javalin.community.openapi:javalin-openapi-plugin:$openapi") // for /openapi route with JSON scheme
    implementation("io.javalin.community.openapi:javalin-swagger-plugin:$openapi") // for Swagger UI

    implementation(libs.bundles.jackson)

    // database
    implementation("org.postgresql", "postgresql", "42.7.9")
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
            var shared = listOf("-Dbot.cleanup=false",
                    "-Dbot.config=config/config.testing.json",
                    "-Dlog4j2.configurationFile=docker/config/log4j2.testing.xml",
                    "-Dcjda.localisation.error.name=false",
                    "-Dcjda.interactions.cleanguildcommands=true",
                    "-Dcjda.interactions.testmode=true",
                    "-Dbot.db.host=localhost,",
                    "-Dbot.api.url=http://localhost:5173")
            runConfigurations {
                register<org.jetbrains.gradle.ext.Application>("App-Testing") {
                    mainClass = "de.chojo.repbot.ReputationBot"
                    jvmArgs = shared.joinToString(" ")
                    moduleName = "rep-bot.main"
                }
                register<org.jetbrains.gradle.ext.Application>("App-Testing - All SKUs") {
                    mainClass = "de.chojo.repbot.ReputationBot"
                    jvmArgs = (shared + "-Dbot.grantallsku=true" + "-Dcjda.premium.skipEntitledCheck=true").joinToString(" ")
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
        useJUnitPlatform {
            excludeTags("locale", "database")
        }
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    register<Test>("testLocale") {
        group = "verification"
        description = "Runs locale validation tests"
        testClassesDirs = sourceSets.test.get().output.classesDirs
        classpath = sourceSets.test.get().runtimeClasspath
        useJUnitPlatform {
            includeTags("locale")
        }
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    register<Test>("testDatabase") {
        group = "verification"
        description = "Runs database validation tests"
        testClassesDirs = sourceSets.test.get().output.classesDirs
        classpath = sourceSets.test.get().runtimeClasspath
        useJUnitPlatform {
            includeTags("database")
        }
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    register("checkLicenseBackend") {
        group = "verification"
        description = "Checks license headers for backend Java files"
        dependsOn("spotlessJavaCheck")
    }

    register("checkLicenseFrontend") {
        group = "verification"
        description = "Checks license headers for frontend Vue and JavaScript files"
        dependsOn("spotlessJavascriptCheck", "spotlessVueCheck")
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
