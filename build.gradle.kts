import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

plugins {
    java
    alias(libs.plugins.shadow)
    alias(libs.plugins.spotless)
    alias(libs.plugins.idea.ext)
}

repositories {
    mavenCentral()
    maven("https://eldonexus.de/repository/maven-public/")
}

spotless {
    java {
        target("src/**/*.java")
        licenseHeaderFile(rootProject.file("HEADER.txt"))
        trimTrailingWhitespace()
        endWithNewline()
        palantirJavaFormat(libs.versions.palantir.get()).formatJavadoc(false)
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
    implementation(libs.cjda.util) {
        exclude(group = "club.minnced", module = "opus-java")
    }

    annotationProcessor(libs.javalin.openapi.processor)
    implementation(libs.bundles.javalin.openapi)

    implementation(libs.jackson.jsr310)
    implementation(libs.re2j)

    implementation(libs.postgresql)
    implementation(libs.bundles.sadu)

    implementation(libs.bundles.log4j)
    implementation(libs.log.util) {
        exclude("org.apache.logging.log4j")
    }

    implementation(libs.xchart)
    implementation(libs.commons.validator)
    implementation(libs.angus.mail)
    implementation(libs.bundles.commonmark)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit)
    testRuntimeOnly(libs.junit.launcher)
    testImplementation(libs.slf4j.simple)
    testImplementation(libs.sadu.testing)
    testImplementation(libs.bundles.testcontainers)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
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
                    "-Dbot.db.host=localhost",
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


val projectVersion = project.version.toString()
tasks {
    processResources {
        inputs.property("projectVersion", projectVersion)
        from(sourceSets.main.get().resources.srcDirs) {
            filesMatching("version") {
                var version = projectVersion
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
        options.release = 25
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

    register<JavaExec>("run") {
        group = "application"
        description = "Runs the bot from the current sources"
        mainClass = "de.chojo.repbot.ReputationBot"
        classpath = sourceSets.main.get().runtimeClasspath
        systemProperties(providers.systemPropertiesPrefixedBy("bot.").get()
                + providers.systemPropertiesPrefixedBy("cjda.").get()
                + providers.systemPropertiesPrefixedBy("log4j2.").get())
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
