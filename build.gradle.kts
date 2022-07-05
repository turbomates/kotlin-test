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

group = "com.github.turbomates"
version = "0.1.2"

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
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xcontext-receivers"
        )
    }
}
detekt {
    toolVersion = deps.versions.detekt.get()
    autoCorrect = false
    parallel = true
    config = files("detekt.yml")
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
            from(components["java"])
        }
    }
}

// publishing {
//     publications {
//         create<MavenPublication>("mavenJava") {
//             artifactId = "test-support"
//             groupId = "com.turbomates"
//             from(components["java"])
//             pom {
//                 packaging = "jar"
//                 name.set("Kotlin test support")
//                 url.set("https://github.com/turbomates/test-support")
//                 description.set("Kotlin test support")
//
//                 licenses {
//                     license {
//                         name.set("MIT License")
//                         url.set("https://github.com/turbomates/test-support/blob/main/LICENSE")
//                     }
//                 }
//                 scm {
//                     connection.set("scm:https://github.com/turbomates/test-support.git")
//                     developerConnection.set("scm:git@github.com:turbomates/test-support.git")
//                     url.set("https://github.com/turbomates/test-support")
//                 }
//                 developers {
//                     developer {
//                         id.set("no-ivan")
//                         name.set("Ivan Novikov")
//                         email.set("novikov.ivan.work@gmail.com")
//                     }
//                 }
//             }
//         }
//     }
//     repositories {
//         maven {
//             val releasesUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
//             val snapshotsUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
//             url = if (version.toString().endsWith("SNAPSHOT")) snapshotsUrl else releasesUrl
//             credentials {
//                 username = System.getenv("ORG_GRADLE_PROJECT_SONATYPE_USERNAME") ?: project.properties["ossrhUsername"].toString()
//                 password = System.getenv("ORG_GRADLE_PROJECT_SONATYPE_PASSWORD") ?: project.properties["ossrhPassword"].toString()
//             }
//         }
//     }
// }

// signing {
//     sign(publishing.publications["mavenJava"])
// }

// nexusStaging {
//     serverUrl = "https://s01.oss.sonatype.org/service/local/"
//     username = System.getenv("ORG_GRADLE_PROJECT_SONATYPE_USERNAME") ?: project.properties["ossrhUsername"].toString()
//     password = System.getenv("ORG_GRADLE_PROJECT_SONATYPE_PASSWORD") ?: project.properties["ossrhPassword"].toString()
// }
