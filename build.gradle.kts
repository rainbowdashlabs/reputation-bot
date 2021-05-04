import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "7.0.0"
    java
    `maven-publish`
    id("org.panteleyev.jpackageplugin") version "1.3.1"
    // id("org.beryx.jlink") version "2.23.8"
}

group = "de.chojo"
version = "1.0"


val log4jVersion = "2.14.0"
val lombokVersion = "1.18.20"

java.sourceCompatibility = JavaVersion.VERSION_15
java.targetCompatibility = JavaVersion.VERSION_15

repositories {
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
    maven {
        url = uri("https://eldonexus.de/repository/maven-releases")
    }
    mavenCentral()
    maven {
        name = "m2-dv8tion"
        url = uri("https://m2.dv8tion.net/releases")
    }
}

dependencies {
    // discord
    implementation("net.dv8tion", "JDA", "4.2.1_259") {
        exclude(module = "opus-java")
    }
    implementation("de.chojo", "cjda-util", "1.0.0")
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

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks {
    named<ShadowJar>("shadowJar") {
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "de.chojo.repbot.ReputationBot"))
        }
    }

    compileJava{
        options.encoding = "UTF-8"
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}

/*jlink {
    addOptions("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages")
    launcher{
        name = "app"
        jvmArgs = listOf("-Dlog4j.configurationFile=config/log4j2.xml", "-Dbot.config=config/config.json")
    }
}*/

task("copyDependencies", Copy::class) {
    from(configurations.runtimeClasspath).into("$buildDir/jars")
}

task("copyJar", Copy::class) {
    from(tasks.jar).into("$buildDir/jars")
}

tasks.jpackage {
    dependsOn("build", "copyDependencies", "copyJar")

    input  = "$buildDir/libs"
    destination = "$buildDir/dist"

    appName = "Reputation Bot"
    vendor = "app.org"

    mainJar = tasks.shadowJar.get().archiveFileName.get()
    mainClass = "de.chojo.repbot.ReputationBot"

    javaOptions = listOf("-Dfile.encoding=UTF-8")

    windows {
        winConsole = true
    }
}
