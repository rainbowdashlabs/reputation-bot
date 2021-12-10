plugins {
    id("com.github.johnrengelman.shadow") version "7.1.0"
    java
    `maven-publish`
}

group = "de.chojo"
version = "1.5.4"

val log4jVersion = "2.15.0"


repositories {
    maven("https://eldonexus.de/repository/maven-public")
    maven("https://eldonexus.de/repository/maven-proxies")
    maven("https://m2.dv8tion.net/releases")
}

dependencies {
    // discord
    implementation("net.dv8tion", "JDA", "4.3.0_339") {
        exclude(module = "opus-java")
    }

    implementation("de.chojo", "cjda-util", "1.5.5")

    // database
    implementation("org.postgresql", "postgresql", "42.3.1")
    implementation("com.zaxxer", "HikariCP", "4.0.3")

    // Serialization
    implementation("com.fasterxml.jackson.core", "jackson-databind", "2.12.3")

    // Logging
    implementation("org.slf4j", "slf4j-api", "1.7.30")
    implementation("org.apache.logging.log4j", "log4j-core", log4jVersion)
    implementation("org.apache.logging.log4j", "log4j-slf4j-impl", log4jVersion)
    implementation("club.minnced", "discord-webhooks", "0.5.8")

    // utils
    implementation("org.apache.commons", "commons-lang3", "3.12.0")
    implementation("de.chojo", "sql-util", "1.1.5")
    implementation("com.google.guava","guava","30.1.1-jre")

    // unit testing
    testImplementation(platform("org.junit:junit-bom:5.7.1"))
    testImplementation("org.junit.jupiter", "junit-jupiter")
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

    shadowJar{
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "de.chojo.repbot.ReputationBot"))
        }
    }

    build {
        dependsOn(shadowJar)
    }
}
