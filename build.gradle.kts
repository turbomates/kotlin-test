import java.time.Duration
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    kotlin("jvm").version(deps.versions.kotlin.asProvider().get())
    alias(deps.plugins.detekt)
    alias(deps.plugins.nexus.release)
    alias(deps.plugins.kotlin.serialization)
    `maven-publish`
    signing
}

group = "com.turbomates"
version = "0.3.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(deps.mockk)
    testImplementation(deps.ktor.server.content.negotiation)
    testImplementation(deps.ktor.serialization.kotlinx.json)
    testImplementation(deps.serialization.json)
    testImplementation(deps.ktor.server.test.host) { exclude(group = "ch.qos.logback", module = "logback-classic") }
    implementation(deps.ktor.server.test.host) { exclude(group = "ch.qos.logback", module = "logback-classic") }
    implementation(deps.ktor.client.core)
    implementation(deps.ktor.client.content.negotiation)
    implementation(deps.ktor.serialization.kotlinx.json)
    implementation(deps.kotlin.serialization)
    implementation(deps.serialization.json)
    implementation(deps.bundles.exposed)
    implementation(deps.h2.database)
    api(deps.kotest)
    api(deps.kotest.jvm)
    detektPlugins(deps.detekt.formatting)
    implementation(kotlin("stdlib-jdk8"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs.set(
            listOf(
                "-opt-in=kotlin.ExperimentalStdlibApi",
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlinx.serialization.InternalSerializationApi",
                "-opt-in=kotlin.contracts.ExperimentalContracts",
                "-Xskip-prerelease-check",
                "-Xcontext-parameters"
            )
        )
    }
}
detekt {
    toolVersion = deps.versions.detekt.get()
    autoCorrect = false
    parallel = true
    config.setFrom("detekt.yml")
}
tasks.named("check").configure {
    this.setDependsOn(this.dependsOn.filterNot {
        it is TaskProvider<*> && it.name == "detekt"
    })
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "ktor-audit"
            groupId = "com.turbomates"
            version = System.getenv("RELEASE_VERSION") ?: "0.1.0"
            from(components["java"])
            pom {
                packaging = "jar"
                name.set("Ktor Audit extensions")
                url.set("https://github.com/turbomates/ktor-audit")
                description.set("Extensions for Hoplite config library")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://github.com/turbomates/ktor-audit/blob/main/LICENSE")
                    }
                }

                scm {
                    connection.set("scm:https://github.com/turbomates/ktor-audit.git")
                    developerConnection.set("scm:git@github.com:turbomates/ktor-audit.git")
                    url.set("https://github.com/turbomates/ktor-audit")
                }

                developers {
                    developer {
                        id.set("shustrik")
                        name.set("Vadim Golodko")
                        email.set("vadim.golodko@gmail.com")
                    }
                }
            }
        }
    }
}
nexusPublishing {
    repositories {
        sonatype {
            // Central Portal OSSRH Staging API URLs
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))

            username.set(
                System.getenv("ORG_GRADLE_PROJECT_SONATYPE_USERNAME")
                    ?: project.findProperty("centralPortalUsername")?.toString()
            )
            password.set(
                System.getenv("ORG_GRADLE_PROJECT_SONATYPE_PASSWORD")
                    ?: project.findProperty("centralPortalPassword")?.toString()
            )
        }
    }

    // Настройки тайм-аутов (опционально)
    connectTimeout.set(Duration.ofMinutes(3))
    clientTimeout.set(Duration.ofMinutes(6))

    transitionCheckOptions {
        maxRetries.set(80)
        delayBetween.set(Duration.ofSeconds(10))
    }
}
signing {
    sign(publishing.publications["mavenJava"])
}
