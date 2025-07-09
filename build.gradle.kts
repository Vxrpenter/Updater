plugins {
    kotlin("jvm") version "2.2.0"
}

group = "io.github.vxrpenter"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    api(libs.okhttp)
    api(libs.slf4j.api)
}

tasks.test {
    useJUnitPlatform()
}