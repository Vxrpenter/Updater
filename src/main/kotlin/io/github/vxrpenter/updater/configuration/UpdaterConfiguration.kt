/*
 * Copyright (c) 2025 Vxrpenter and the Updater contributors
 *
 * Licenced under the MIT License, any non-license compliant usage of this file(s) content
 * is prohibited. If you did not receive a copy of the license with this file, you
 * may obtain the license at
 *
 *  https://mit-license.org/
 *
 * This software may be used commercially if the usage is license compliant. The software
 * is provided without any sort of WARRANTY, and the authors cannot be held liable for
 * any form of claim, damages or other liabilities.
 *
 * Note: This is no legal advice, please read the license conditions
 */

package io.github.vxrpenter.updater.configuration

import kotlin.time.Duration
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttpEngine
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

/**
 * The updater configuration
 */
data class UpdaterConfiguration(
    /**
     * The JSON deserializer used by the [HttpClient]
     */
    val json: Json = Json { ignoreUnknownKeys = true },
    /**
     * Defines the time between periodic version checks
     */
    val periodic: Duration? = null,
    /**
     * The [HttpClient] (with [OkHttpEngine]) read timout
     */
    val readTimeout: Duration = 30.seconds,
    /**
     * The [HttpClient] (with [OkHttpEngine]) write timout
     */
    val writeTimeout: Duration = 30.seconds,
    /**
     * The notification settings
     */
    val notification: UpdaterConfigurationNotification = UpdaterConfigurationNotification(true)
)

/**
 * The updater configurations notification configuration
 */
data class UpdaterConfigurationNotification(
    /**
     * Should a new update prompt a notification?
     */
    val notify: Boolean,
    /**
     * Message that will be prompted when a new version has been found
     */
    val notification: String = "New update has been found. Version {version} can be downloaded from {url}"
)