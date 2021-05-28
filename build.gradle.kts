import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "6.0.0"
    java
    `maven-publish`
}

group = "de.chojo"
version = "1.1.1"

val log4jVersion = "2.14.0"
val lombokVersion = "1.18.20"


repositories {
    maven("https://eldonexus.de/repository/maven-proxies")
    maven {
        url = uri("https://eldonexus.de/repository/maven-public")
    }
    maven {
        name = "m2-dv8tion"
        url = uri("https://m2.dv8tion.net/releases")
    }
}

dependencies {
    // discord
    implementation("com.github.DV8FromTheWorld", "JDA", "9c31ef1504") {
        exclude(module = "opus-java")
    }
    implementation("de.chojo", "cjda-util", "1.1.0-DEV") {
        isTransitive = false
    }

    // database
    implementation("org.postgresql", "postgresql", "42.2.19")
    implementation("com.zaxxer", "HikariCP", "4.0.3")

    // Serialization
    implementation("com.fasterxml.jackson.core", "jackson-databind", "2.12.3")

    // Logging
    implementation("org.slf4j", "slf4j-api", "1.7.30")
    implementation("org.apache.logging.log4j", "log4j-core", log4jVersion)
    implementation("org.apache.logging.log4j", "log4j-slf4j-impl", log4jVersion)

    // annotation processing
    compileOnly("org.projectlombok", "lombok", lombokVersion)
    annotationProcessor("org.projectlombok", "lombok", lombokVersion)

    // utils
    implementation("com.kcthota", "emoji4j", "6.0")
    implementation("org.apache.commons", "commons-lang3", "3.12.0")

    // unit testing
    testImplementation(platform("org.junit:junit-bom:5.7.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testCompileOnly("org.projectlombok", "lombok", lombokVersion)
    testAnnotationProcessor("org.projectlombok", "lombok", lombokVersion)
}


java {
    withSourcesJar()
    withJavadocJar()

    sourceCompatibility = JavaVersion.VERSION_15
}

tasks {
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
