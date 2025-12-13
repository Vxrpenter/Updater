import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URI

buildscript {
    dependencies {
        classpath(libs.dokka.base)
    }
}

plugins {
    alias(libs.plugins.dokka)
    alias(libs.plugins.mavenpublish)
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
}

tasks.getByName("dokkaHtml", DokkaTask::class) {
    dokkaSourceSets.configureEach {
        includes.from("packages.md")
        jdkVersion.set(8)
        sourceLink {
            localDirectory.set(file("src/master/kotlin"))
            remoteUrl.set(URI("https://github.com/Vxrpenter/Updater/tree/master/src/main/kotlin").toURL())
            remoteLineSuffix.set("#V")
        }
    }

    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
        footerMessage = "Copyright © 2025 Vxrpenter"
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    coordinates(group.toString(), "updater")

    pom {
        name = "Updater"
        description = "A library for secure and modular update management "
        inceptionYear = "2025"
        url = "https://github.com/Vxrpenter/Updater"
        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/license/mit"
            }
        }
        developers {
            developer {
                name = "Vxrpenter"
                url = "https://github.com/Vxrpenter"
            }
        }
        scm {
            url = "https://github.com/Vxrpenter/Updater"
            connection = "scm:git:git://github.com/Vxrpenter/Updater.git"
            developerConnection = "scm:git:ssh://git@github.com/Vxrpenter/Updater.git"
        }
    }
}