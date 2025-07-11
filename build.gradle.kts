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
    api(libs.okhttp)
    api(libs.slf4j.api)
    api(libs.kotlinx.serialization)
    api(libs.kotlinx.coroutines)
    implementation("ch.qos.logback:logback-classic:1.5.18")
}

tasks.test {
    useJUnitPlatform()
}