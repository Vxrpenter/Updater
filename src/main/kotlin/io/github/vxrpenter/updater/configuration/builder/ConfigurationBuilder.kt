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

@file:Suppress("unused", "FunctionName")

package io.github.vxrpenter.updater.configuration.builder

import io.github.vxrpenter.updater.configuration.UpdaterConfiguration
import io.github.vxrpenter.updater.configuration.UpdaterConfigurationNotification
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import kotlinx.serialization.json.Json
import kotlin.time.Duration

/**
 * The [Configuration] function is an easy way to creating a [io.github.vxrpenter.updater.configuration.UpdaterConfiguration], by providing simple solutions and
 * an easy-to-understand format.
 * If you want to use a more complex function, you can use the [ConfigurationBuilder].
 *
 * Example Usage:
 * ```kotlin
 * val configuration = Configuration {
 *     periodic = 1.hours
 *     readTimeout {
 *         timeout = 120
 *         unit = TimeUnit.SECONDS
 *     }
 *     writeTimeout {
 *         timeout = 120
 *         unit = TimeUnit.SECONDS
 *     }
 *     newUpdateNotification = "New version {} is now available and can be downloaded from {}"
 * }
 * ```
 *
 * @param [ConfigurationBuilder.json] The JSON deserializer used by the [HttpClient]
 * @param [ConfigurationBuilder.periodic] Defines the time between periodic version checks
 * @param [ConfigurationBuilder.readTimeout] The [HttpClient] (with [OkHttpEngine]) read timout
 * @param [ConfigurationBuilder.writeTimeout] The [HttpClient] (with [OkHttpEngine]) write timout
 * @param [ConfigurationBuilder.notification] The notification settings
 *
 * @return the [io.github.vxrpenter.updater.configuration.UpdaterConfiguration]
 * @see ConfigurationBuilder
 */
inline fun Configuration(
    builder: ConfigurationBuilder.() -> Unit
): UpdaterConfiguration {
    val internalBuilder = ConfigurationBuilder()
    internalBuilder.builder()
    val configuration = internalBuilder.build()
    return configuration
}

class ConfigurationBuilder {
    private val defaultConfig = UpdaterConfiguration()
    /**
     * The JSON deserializer used by the [HttpClient]
     */
    var json: Json = defaultConfig.json

    /**
     * Defines the time between periodic version checks
     */
    var periodic: Duration? = null

    /**
     * The [HttpClient] (with [OkHttpEngine]) read timout
     */
    var readTimeout: Duration = defaultConfig.readTimeout

    /**
     * The [HttpClient] (with [OkHttpEngine]) write timout
     */
    var writeTimeout: Duration = defaultConfig.writeTimeout

    /**
     * The notification settings
     */
    private var notification: UpdaterConfigurationNotification = defaultConfig.notification

    /**
     * The [HttpClient] (with [OkHttpEngine]) read timout
     */
    fun notification(
        builder: InlineUpdaterConfigurationNotification.() -> Unit
    ) {
        val inlineNotification = InlineUpdaterConfigurationNotification().apply(builder)
        requireNotNull(inlineNotification.notify)
        requireNotNull(inlineNotification.message)

        notification = UpdaterConfigurationNotification(
            notify = inlineNotification.notify!!,
            message = inlineNotification.message!!
        )
    }

    data class InlineUpdaterConfigurationNotification(
        /**
         * Should a new update prompt a notification?
         */
        var notify: Boolean? = null,
        /**
         * Message that will be prompted when a new version has been found
         */
        var message: String? = null
    )

    fun build(): UpdaterConfiguration {
        return UpdaterConfiguration(json, periodic, readTimeout, writeTimeout, notification)
    }
}