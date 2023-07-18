rootProject.name = "rep-bot"

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.6.0")
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            // misc
            version("sadu", "1.3.0")
            library("sadu-queries", "de.chojo.sadu", "sadu-queries").versionRef("sadu")
            library("sadu-updater", "de.chojo.sadu", "sadu-updater").versionRef("sadu")
            library("sadu-postgresql", "de.chojo.sadu", "sadu-postgresql").versionRef("sadu")
            library("sadu-datasource", "de.chojo.sadu", "sadu-datasource").versionRef("sadu")
            bundle("sadu", listOf("sadu-queries", "sadu-updater", "sadu-postgresql", "sadu-datasource"))

            version("log4j", "2.20.0")
            library("slf4j-api", "org.slf4j:slf4j-api:2.0.7")
            library("log4j-core", "org.apache.logging.log4j", "log4j-core").versionRef("log4j")
            library("log4j-slf4j2", "org.apache.logging.log4j", "log4j-slf4j2-impl").versionRef("log4j")
            bundle("log4j", listOf("slf4j-api", "log4j-core", "log4j-slf4j2"))

            // plugins
            plugin("spotless", "com.diffplug.spotless").version("6.20.0")
            plugin("shadow", "com.github.johnrengelman.shadow").version("8.1.1")

        }
    }
}
