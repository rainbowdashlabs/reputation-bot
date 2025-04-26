import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer

plugins {
    alias(libs.plugins.shadow)
    alias(libs.plugins.spotless)
    java
}

group = "de.chojo"
version = "1.15.2"

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
    //discord
    implementation("de.chojo", "cjda-util", "2.11.0+jda-5.3.2-SNAPSHOT") {
        exclude(group = "club.minnced", module = "opus-java")
    }

    val openapi = "6.6.0"

    annotationProcessor("io.javalin.community.openapi:openapi-annotation-processor:$openapi")
    implementation("io.javalin.community.openapi:javalin-openapi-plugin:$openapi") // for /openapi route with JSON scheme
    implementation("io.javalin.community.openapi:javalin-swagger-plugin:$openapi") // for Swagger UI

    // database
    implementation("org.postgresql", "postgresql", "42.7.5")
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
<<<<<<< Updated upstream
=======
    testImplementation(libs.sadu.testing)
    testImplementation(testlibs.bundles.database.postgres)
    testImplementation(testlibs.slf4j.noop)
>>>>>>> Stashed changes
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
    withJavadocJar()
}

tasks {
    processResources {
        from(sourceSets.main.get().resources.srcDirs) {
            filesMatching("version") {
                expand(
                    "version" to project.version
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
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "de.chojo.repbot.ReputationBot"))
        }
    }

    build {
        dependsOn(shadowJar)
    }
}
