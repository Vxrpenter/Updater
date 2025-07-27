plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
}

group = "io.github.vxrpenter"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    api(libs.kotlin.logging)
    api(libs.kotlinx.serialization)
    api(libs.kotlinx.coroutines)

    api(libs.ktor.client.core)
    api(libs.ktor.client.engine.okhttp)
    api(libs.ktor.client.content.negotiation)
    api(libs.ktor.serialization.kotlinx.json)

    // Implementation for testing
    implementation("ch.qos.logback:logback-classic:1.5.18")
}

tasks.test {
    useJUnitPlatform()
}