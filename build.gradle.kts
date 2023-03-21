plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    java
    `maven-publish`
}

group = "de.chojo"
version = "1.13.9"

repositories {
    maven("https://eldonexus.de/repository/maven-public")
    maven("https://eldonexus.de/repository/maven-proxies")
    maven("https://m2.dv8tion.net/releases")
}

dependencies {
    //discord
    implementation("de.chojo", "cjda-util", "2.8.3+beta.5") {
        exclude(group = "club.minnced", module = "opus-java")
    }

    // database
    implementation("org.postgresql", "postgresql", "42.6.0")
    implementation("de.chojo.sadu", "sadu-queries", "1.2.0")
    implementation("de.chojo.sadu", "sadu-updater", "1.2.0")
    implementation("de.chojo.sadu", "sadu-postgresql", "1.2.0")
    implementation("de.chojo.sadu", "sadu-datasource", "1.2.0")

    // Logging
    implementation("org.slf4j", "slf4j-api", "2.0.7")
    implementation("org.apache.logging.log4j", "log4j-core", "2.20.0")
    implementation("org.apache.logging.log4j", "log4j-slf4j2-impl", "2.20.0")
    implementation("club.minnced", "discord-webhooks", "0.8.2")

    implementation("org.knowm.xchart", "xchart", "3.8.3")

    // unit testing
    testImplementation(platform("org.junit:junit-bom:5.9.2"))
    testImplementation("org.junit.jupiter", "junit-jupiter")
    testImplementation("org.knowm.xchart", "xchart", "3.8.3")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
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
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "de.chojo.repbot.ReputationBot"))
        }
    }

    build {
        dependsOn(shadowJar)
    }
}
