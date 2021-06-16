import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "6.0.0"
    java
    `maven-publish`
}

group = "de.chojo"
version = "1.2.2"

val log4jVersion = "2.14.0"
val lombokVersion = "1.18.20"


repositories {
    maven("https://eldonexus.de/repository/maven-proxies")
    maven("https://eldonexus.de/repository/maven-public")
    maven("https://m2.dv8tion.net/releases")
}

dependencies {
    // discord
    implementation("net.dv8tion", "JDA", "4.2.1_269") {
        exclude(module = "opus-java")
    }
    implementation("de.chojo", "cjda-util", "1.3.2-DEV")

    // database
    implementation("org.postgresql", "postgresql", "42.2.19")
    implementation("com.zaxxer", "HikariCP", "4.0.3")

    // Serialization
    implementation("com.fasterxml.jackson.core", "jackson-databind", "2.12.3")

    // Logging
    implementation("org.slf4j", "slf4j-api", "1.7.30")
    implementation("org.apache.logging.log4j", "log4j-core", log4jVersion)
    implementation("org.apache.logging.log4j", "log4j-slf4j-impl", log4jVersion)
    implementation("club.minnced", "discord-webhooks", "0.5.7")

    // utils
    implementation("org.apache.commons", "commons-lang3", "3.12.0")

    // unit testing
    testImplementation(platform("org.junit:junit-bom:5.7.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}


java {
    withSourcesJar()
    withJavadocJar()

    sourceCompatibility = JavaVersion.VERSION_15
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
    named<ShadowJar>("shadowJar") {
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "de.chojo.repbot.ReputationBot"))
        }
    }

    build {
        dependsOn(shadowJar)
    }
}
