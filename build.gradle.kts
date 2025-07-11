plugins {
    kotlin("jvm") version "2.2.0"
    libs.plugins.kotlin.serialization
}

group = "io.github.vxrpenter"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    api(libs.okhttp)
    api(libs.slf4j.api)
    api(libs.kotlinx.serialization)
    api(libs.kotlinx.coroutines)
}

tasks.test {
    useJUnitPlatform()
}