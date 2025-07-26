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
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

interface UpdaterInterface {
    var configuration: UpdaterConfiguration
    var client: HttpClient

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

    suspend fun default(currentVersion: Version, schema: UpdateSchema, upstream: Upstream, builder: (ConfigurationBuilder.() -> Unit)? = null)
    suspend fun multiUpstream(currentVersion: Version, schema: UpdateSchema, upstreams: Collection<Upstream>, builder: (ConfigurationBuilder.() -> Unit)? = null)

    fun runBuilder(builder: ConfigurationBuilder.() -> Unit) {
        val internalBuilder = ConfigurationBuilder()
        internalBuilder.builder()
        configuration = internalBuilder.build()
        client = createClient()
    }
}