<img align="left" src="https://github.com/Vxrpenter/Updater/blob/assets/assets/logo.png" width="100" height="100"/>

<br/>

# Updater
<div align="left">
  <a href="https://github.com/Vxrpenter/Updater/releases"><img src="https://img.shields.io/github/v/release/Vxrpenter/Updater?include_prereleases&logo=github&logoSize=amg&logoColor=white&labelColor=333834&sort=date&display_name=tag&style=flat-square&label=Latest%20Release&color=forestgreen" /></a>&nbsp;
  <a href="https://github.com/Vxrpenter/Updater/blob/master/LICENSE"><img src="https://img.shields.io/github/license/Vxrpenter/Updater?style=flat-square&logo=amazoniam&logoSize=amg&logoColor=forestgreen&label=Licenced%20Under&labelColor=333834&color=077533"/></a>&nbsp;
  <a href="https://vxrpenter.github.io/Updater-Wiki/"><img src="https://img.shields.io/badge/Wiki-Online-forestgreen?style=flat-square&logo=materialformkdocs&logoSize=amg"/></a>&nbsp;
  <a href="https://vxrpenter.github.io/Updater/"><img src="https://img.shields.io/badge/KDoc-Online-forestgreen?style=flat-square&logo=kotlin&logoSize=amg"/></a>&nbsp;
</div>

## What is Updater?
Updater is a kotlin (java) library for update management. It has integration with platforms like *GitHub*, *Modrinth*, *Spigot* and more to allow easy setup without much work. It allows users to specify a custom version schema that allows you to customize your versions as you like.

## Installation

<a href=""><img src="https://img.shields.io/maven-central/v/io.github.vxrpenter/updater?style=flat-square&logo=apachemaven&logoColor=f18800&color=f18800"></a>

### Gradle
```gradle
dependencies {
  implementation("io.github.vxrpenter:updater:VERSION")
}
```

### Maven
```xml
<dependency>
    <groupId>io.github.vxrpenter</groupId>
    <artifactId>updater</artifactId>
    <version>VERSION</version>
</dependency>
```
*Replace `VERSION` with the latest version*

## Getting Started

This is a small usage example that tries to outline the core functionality of the library.

### Creating a Schema

To begin,
we first have to create an UpdateSchema 
that allows the library to deserialize your versions into readable components and classifiers. 
The example below shows how such a schema could look.
It uses the `Schema` function which uses the `SchemaBuilder` to create a `DefaultSchema`. 
Most upstreams will accept a `DefaultSchema` but some will beed specific schema types, so keep an eye out for that. 

The classifiers that can be added using the `SchemaBuilder` are `DefaultClassifiers` but some upstreams wil require custom classifiers so also keep an eye out for that.
```kotlin
val schema = Schema {
    // The prefix stands before the actual version, e.g. 'v1.0.0'
    prefix = "v"
    // The symbol that divides the version numbers
    divider = "."
    // A classifier is an argument that can be added to a version that defines if it's a 'special' version
    // like an alpha or beta release
    classifier {
        // The classifier identifier value
        value = "a"
        // The divider between identifier value and version number
        divider = "-"
        // Divider between the components
        componentDivider = "."
        // The priority that the classifier has in comparison to other classifiers
        priority = ClassifierPriority.LOW
    }
    // Some extra classifiers for showcase
    classifier {
        value = "b"
        divider = "-"
        componentDivider = "."
        priority = ClassifierPriority.HIGH
    }
    classifier {
        value = "rc"
        divider = "-"
        componentDivider = "."
        priority = ClassifierPriority.HIGHEST
    }
}
```

### Configuring the Upstream

The next step will be configuring the upstream (the location that we upload our versions). In this example we will use GitHub as our upstream. 
You will need to enter certain information needed to fetch your project from the upstream's api.
```kotlin
val upstream = GithubUpstream(user = "Vxrpenter", repo = "Updater")
```

### Checking for Updates

The last thing will be to check for new versions. This can be easily achived by invoking the `Updater` class and then calling the `checkUpdates` function.
It will require you to enter the current version of your project (if you want to know how to get the current version, look [here](https://github.com/Vxrpenter/Updater?tab=readme-ov-file#current-version-fetching--gradle-only) followed by
the `´UpdateSchema` and the `Upstream`.

You are also able to configure certain behaviors of the `Updater` like adding a periodic check, customizing the notification message, configuring the read/write timeout, etc.
```kotlin
Updater.checkUpdates(currentVersion = "v1.0.0", schema = schema, upstream = upstream) {
    periodic = 10.minutes
    notification {
        notify = true
        notification = "A new version has arrived. Version {version} can be downloaded the link {url}"
    }
}
```

## Current Version Fetching | *Gradle Only*

The easiest way to get the current version of your project from the `build.gradle.kts` is by adding a task to create a properties file.
This file will be created when the project is compiled and can be read at runtime. First, we will need to set up the task to create the properties file:
```kotlin
val createVersionProperties by tasks.registering(WriteProperties::class) {
    val filePath = sourceSets.main.map {
        it.output.resourcesDir!!.resolve("${layout.buildDirectory}/resources/version.properties")
    }
    destinationFile = filePath

    property("version", project.version.toString())
}

tasks.classes {
    dependsOn(createVersionProperties)
}
```

To get the version from the properties file at runtime, you will need to first load the properties file and then retrieve the property `version` from it:
```kotlin
class TestClass {
    fun main() {
        val properties = Properties()

        TestClass::class.java.getResourceAsStream("DIRECTORY/resources/version.properties").use {
                versionPropertiesStream -> checkNotNull(versionPropertiesStream) { "Version properties file does not exist" }
            properties.load(InputStreamReader(versionPropertiesStream, StandardCharsets.UTF_8))
        }

        val version = properties.getProperty("version")
    }
}
```
