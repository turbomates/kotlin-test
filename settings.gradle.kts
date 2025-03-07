pluginManagement {
    plugins {
        kotlin("jvm") version "2.1.10"
    }
}
rootProject.name = "test-support"
include("src")

dependencyResolutionManagement {
    versionCatalogs {
        create("deps") {
            version("ktor", "2.3.13")
            version("detekt", "1.23.6")
            version("kotlin", "2.1.10")
            version("kotlin_serialization_json", "1.8.0")
            version("exposed", "0.60.0")
            version("h2database", "2.1.214")
            version("kotest", "5.9.1")
            version("mockk", "1.13.11")

            library("ktor_client_core", "io.ktor", "ktor-client-core").versionRef("ktor")
            library("ktor_server_content_negotiation", "io.ktor", "ktor-server-content-negotiation").versionRef("ktor")
            library("ktor_client_content_negotiation", "io.ktor", "ktor-client-content-negotiation").versionRef("ktor")
            library("ktor_serialization_kotlinx_json", "io.ktor", "ktor-serialization-kotlinx-json").versionRef("ktor")
            library("serialization_json", "org.jetbrains.kotlinx", "kotlinx-serialization-json").versionRef("kotlin_serialization_json")
            library("kotlin_test", "org.jetbrains.kotlin", "kotlin-test-junit5").versionRef("kotlin")
            library("ktor_server_test_host", "io.ktor", "ktor-server-test-host").versionRef("ktor")
            library("kotlin_serialization", "org.jetbrains.kotlin", "kotlin-serialization").versionRef("kotlin")
            library("exposed_dao", "org.jetbrains.exposed", "exposed-dao").versionRef("exposed")
            library("exposed_jdbc", "org.jetbrains.exposed", "exposed-jdbc").versionRef("exposed")
            library("exposed_core", "org.jetbrains.exposed", "exposed-core").versionRef("exposed")
            library("h2_database", "com.h2database", "h2").versionRef("h2database")
            library("kotest", "io.kotest", "kotest-assertions-core").versionRef("kotest")
            library("kotest-jvm", "io.kotest", "kotest-assertions-core-jvm").versionRef("kotest")
            library("mockk", "io.mockk", "mockk").versionRef("mockk")
            plugin("detekt", "io.gitlab.arturbosch.detekt").versionRef("detekt")
            library("detekt_formatting", "io.gitlab.arturbosch.detekt", "detekt-formatting").versionRef("detekt")
            plugin("kotlin_serialization", "org.jetbrains.kotlin.plugin.serialization").versionRef("kotlin")

            bundle(
                "exposed",
                listOf(
                    "exposed_dao",
                    "exposed_jdbc",
                    "exposed_core"
                )
            )
        }
    }
}
include("main")
