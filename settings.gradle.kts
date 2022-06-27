rootProject.name = "test-support"
include("src")

enableFeaturePreview("VERSION_CATALOGS")
dependencyResolutionManagement {
    versionCatalogs {
        create("deps") {
            version("ktor", "2.0.2")
            version("detekt", "1.21.0-RC1")
            version("kotlin", "1.6.20")
            version("kotlin_serialization_json", "1.3.1")
            version("nexus_staging", "0.30.0")
            version("exposed", "0.38.2")
            version("h2database", "2.1.214")
            version("kotest", "5.2.3")
            version("mockk", "1.12.4")

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
            library("h2_database", "com.h2database", "h2").versionRef("h2database")
            library("kotest", "io.kotest", "kotest-assertions-core").versionRef("kotest")
            library("kotest-jvm", "io.kotest", "kotest-assertions-core-jvm").versionRef("kotest")
            library("mockk", "io.mockk", "mockk").versionRef("mockk")
            plugin("detekt", "io.gitlab.arturbosch.detekt").versionRef("detekt")
            library("detekt_formatting", "io.gitlab.arturbosch.detekt", "detekt-formatting").versionRef("detekt")
            plugin("nexus_release", "io.codearte.nexus-staging").versionRef("nexus_staging")
            plugin("kotlin_serialization", "org.jetbrains.kotlin.plugin.serialization").versionRef("kotlin")

            bundle(
                "exposed",
                listOf(
                    "exposed_dao",
                    "exposed_jdbc"
                )
            )
        }
    }
}
include("main")
