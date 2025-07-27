/*
 * Copyright (c) 2024 Vxrpenter and the Updater contributors
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

package io.github.vxrpenter.updater.interfaces

import io.github.vxrpenter.updater.builder.ConfigurationBuilder
import io.github.vxrpenter.updater.data.UpdateSchema
import io.github.vxrpenter.updater.data.UpdaterConfiguration
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * A generic update verification interface with the purpose of comparing versions fetched from
 * specific upstreams ([UpstreamInterface]) with other versions.
 */
interface UpdaterInterface {
    /**
     * Configuration of the updater
     */
    var configuration: UpdaterConfiguration

    /**
     * An [HttpClient], that is configured using the [configuration].
     * This client will be passed onto all upstream fetching logic to execute calls.
     */
    var client: HttpClient

    /**
     * Return the [HttpClient] using the [configuration].
     *
     * @return the [HttpClient]
     */
    fun createClient(): HttpClient {
        return HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }

            engine { config {
                readTimeout(configuration.readTimeOut.timeout, configuration.readTimeOut.unit)
                writeTimeout(configuration.readTimeOut.timeout, configuration.readTimeOut.unit)
            }}
        }
    }

    /**
     * The default update comparison.
     * It compares the current version to the one fetched from a configured upstream.
     *
     * @param currentVersion complete version of the application
     * @param schema defines the version deserialization
     * @param builder the builder
     */
    suspend fun default(currentVersion: String, schema: UpdateSchema, upstream: UpstreamInterface, builder: (ConfigurationBuilder.() -> Unit)? = null)

    /**
     * Applies configuration builder
     *
     * @param builder the builder
     */
    fun runBuilder(builder: ConfigurationBuilder.() -> Unit) {
        val internalBuilder = ConfigurationBuilder()
        internalBuilder.builder()
        configuration = internalBuilder.build()
        client = createClient()
    }
}