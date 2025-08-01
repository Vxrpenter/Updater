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

@file:Suppress("unused", "FunctionName")

package io.github.vxrpenter.updater

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.vxrpenter.updater.annotations.ExperimentalScheduler
import io.github.vxrpenter.updater.configuration.ConfigurationBuilder
import io.github.vxrpenter.updater.configuration.UpdaterConfiguration
import io.github.vxrpenter.updater.exceptions.UnsuccessfulVersionFetch
import io.github.vxrpenter.updater.schema.UpdateSchema
import io.github.vxrpenter.updater.upstream.Upstream
import io.github.vxrpenter.updater.version.Version
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json

/**
 * Compares versions fetched from
 * specific upstreaming ([Upstream]) with other versions.
 *
 * @param configuration of the updater
 */

open class Updater(private var configuration: UpdaterConfiguration)  {
    private val logger = KotlinLogging.logger {}
    private val updatesScope = CoroutineScope(CoroutineExceptionHandler { _, exception -> logger.error(exception) { "An error occurred in the update coroutine" } })

    /**
     * An [HttpClient], that is configured using the [configuration].
     * This client will be passed onto all upstream fetching logic to execute calls.
     */
    private var client: HttpClient = createClient()

    /**
     * Return the [HttpClient] using the [configuration].
     *
     * @return the [HttpClient]
     */
    private fun createClient(): HttpClient {
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
     * The default updater object using the default configuration.
     * Configuration can be changed using the builders in the functions.
     */
    companion object Default : Updater(configuration = UpdaterConfiguration())

    /**
     * The default update comparison.
     * It compares the current version to the one fetched from a configured upstream.
     *
     * @param currentVersion complete version of the application
     * @param schema defines the version deserialization
     * @param builder the builder
     */
    @OptIn(ExperimentalScheduler::class)
    suspend fun default(currentVersion: String, schema: UpdateSchema, upstream: Upstream, builder: (ConfigurationBuilder.() -> Unit)? = null) {
        if (builder != null) runBuilder(builder)

        start { innerUpdater(currentVersion = upstream.toVersion(currentVersion, schema), schema = schema, upstream = upstream) }
    }

    @OptIn(ExperimentalScheduler::class)
    private suspend fun start(task: suspend () -> Unit) {
        if (configuration.periodic != null) {
            Timer.schedule(period = configuration.periodic!!, coroutineScope = updatesScope) {
                task()
            }
        } else {
            task()
        }
    }

    private fun runBuilder(builder: ConfigurationBuilder.() -> Unit) {
        val internalBuilder = ConfigurationBuilder()
        internalBuilder.builder()
        configuration = internalBuilder.build()
        client = createClient()
    }

    private suspend fun innerUpdater(currentVersion: Version, schema: UpdateSchema, upstream: Upstream) {
        val version = upstream.fetch(client = client, schema = schema)

        version ?: throw UnsuccessfulVersionFetch("Could not fetch version from upstream", Throwable("Either upstream not available or serializer out of date"))
        if (currentVersion >= version) return

        val update = upstream.update(version)

        logger.warn { configuration.newUpdateNotification
            .replace("{version}", update.value)
            .replace("{url}", update.url)
        }
    }
}