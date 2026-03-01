rootProject.name = "bot"

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version ("1.0.0")
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            // misc
            version("sadu", "2.3.7")
            library("sadu-queries", "de.chojo.sadu", "sadu-queries").versionRef("sadu")
            library("sadu-updater", "de.chojo.sadu", "sadu-updater").versionRef("sadu")
            library("sadu-postgresql", "de.chojo.sadu", "sadu-postgresql").versionRef("sadu")
            library("sadu-datasource", "de.chojo.sadu", "sadu-datasource").versionRef("sadu")
            bundle("sadu", listOf("sadu-queries", "sadu-updater", "sadu-postgresql", "sadu-datasource"))

            version("log4j", "2.25.3")
            library("slf4j-api", "org.slf4j:slf4j-api:2.0.17")
            library("log4j-core", "org.apache.logging.log4j", "log4j-core").versionRef("log4j")
            library("log4j-slf4j2", "org.apache.logging.log4j", "log4j-slf4j2-impl").versionRef("log4j")
            library("log4j-jsontemplate", "org.apache.logging.log4j", "log4j-layout-template-json").versionRef("log4j")
            bundle("log4j", listOf("slf4j-api", "log4j-core", "log4j-slf4j2", "log4j-jsontemplate"))

            version("jackson", "2.21.1")
            library("jackson-jsr310", "com.fasterxml.jackson.datatype","jackson-datatype-jsr310").versionRef("jackson")
            bundle("jackson", listOf("jackson-jsr310"))

            version("commonmark","0.27.1")
            library("commonmark", "org.commonmark", "commonmark").versionRef("commonmark")
            library("commonmark-ext-gfm-tables", "org.commonmark", "commonmark-ext-gfm-tables").versionRef("commonmark")
            library("commonmark-ext-heading-anchor", "org.commonmark", "commonmark-ext-heading-anchor").versionRef("commonmark")
            library("commonmark-ext-autolink", "org.commonmark", "commonmark-ext-autolink").versionRef("commonmark")
            bundle("commonmark", listOf("commonmark", "commonmark-ext-gfm-tables", "commonmark-ext-heading-anchor", "commonmark-ext-autolink"))

            // plugins
            plugin("spotless", "com.diffplug.spotless").version("8.2.1")
            plugin("shadow", "com.gradleup.shadow").version("9.3.2")
        }

        create("testlibs") {
            version("junit", "6.0.3")
            library("junit-jupiter", "org.junit.jupiter", "junit-jupiter").versionRef("junit")
            library("junit-params", "org.junit.jupiter", "junit-jupiter-params").versionRef("junit")
            bundle("junit", listOf("junit-jupiter", "junit-params", "slf4j-simple"))

            version("sadu", "2.3.7")
            library("sadu-testing", "de.chojo.sadu", "sadu-testing").versionRef("sadu")

            version("testcontainers", "2.0.3")
            library("testcontainers-postgres", "org.testcontainers", "testcontainers-postgresql").versionRef("testcontainers")
            library("testcontainers-core", "org.testcontainers", "testcontainers").versionRef("testcontainers")
            library("testcontainers-junit", "org.testcontainers", "testcontainers-junit-jupiter").versionRef("testcontainers")

            version("slf4j", "2.0.17")
            library("slf4j-noop", "org.slf4j", "slf4j-nop").versionRef("slf4j")
            library("slf4j-simple", "org.slf4j", "slf4j-simple").versionRef("slf4j")

            library("driver-postgres", "org.postgresql:postgresql:42.7.10")

            bundle("database-postgres", listOf("testcontainers-junit", "testcontainers-core", "testcontainers-postgres", "driver-postgres"))
        }
    }
}
