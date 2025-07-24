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

package io.github.vxrpenter

import io.github.vxrpenter.annotations.ExperimentalScheduler
import io.github.vxrpenter.builder.ConfigurationBuilder
import io.github.vxrpenter.data.Update
import io.github.vxrpenter.data.UpdateSchema
import io.github.vxrpenter.data.UpdaterConfiguration
import io.github.vxrpenter.data.upstream.Upstream
import io.github.vxrpenter.handler.VersionComparisonHandler
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

sealed class Updater(private var configuration: UpdaterConfiguration) {
    private val logger = LoggerFactory.getLogger(Updater::class.java)
    // Defining client
    private var client: HttpClient = createClient()

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

    // Default configuration object
    companion object Default : Updater(configuration =  UpdaterConfiguration())

    @OptIn(ExperimentalScheduler::class)
    suspend fun default(currentVersion: String, schema: UpdateSchema, upstream: Upstream, builder: ConfigurationBuilder.() -> Unit) {
        // Configuration Builder
        val internalBuilder = ConfigurationBuilder()
        internalBuilder.builder()
        configuration = internalBuilder.build()
        client = createClient()

        // Logic
        if (configuration.periodic != null) {
            val updatesScope = CoroutineScope(CoroutineExceptionHandler { _, exception ->
                LoggerFactory.getLogger(Updater::class.java).error("An error occurred in the update coroutine", exception)
            })

            Timer.schedule(period = configuration.periodic!!, coroutineScope = updatesScope) {
                InnerUpdater(currentVersion = currentVersion, schema = schema, upstream = upstream)
            }
        } else {
            InnerUpdater(currentVersion = currentVersion, schema = schema, upstream = upstream)
        }
    }

    @OptIn(ExperimentalScheduler::class)
    suspend fun multiUpstream(currentVersion: String, schema: UpdateSchema, upstreams: Collection<Upstream>, builder: (ConfigurationBuilder.() -> Unit)? = null) {
        // Configuration Builder
        if (builder != null) {
            val internalBuilder = ConfigurationBuilder()
            internalBuilder.builder()
            configuration = internalBuilder.build()
            client = createClient()
        }
        // Logic
        if (configuration.periodic != null) {
            val updatesScope = CoroutineScope(CoroutineExceptionHandler { _, exception ->
                LoggerFactory.getLogger(Updater::class.java).error("An error occurred in the update coroutine", exception)
            })

            Timer.schedule(period = configuration.periodic!!, coroutineScope = updatesScope) {
                multiUpstreamUpdater(currentVersion = currentVersion, schema = schema, upstreams = upstreams)
            }
        } else {
            multiUpstreamUpdater(currentVersion = currentVersion, schema = schema, upstreams = upstreams)
        }
    }

    private suspend fun multiUpstreamUpdater(currentVersion: String, schema: UpdateSchema, upstreams: Collection<Upstream>) {
        val fetchedUpdates: MutableCollection<Update> = mutableListOf()
        val fetchedVersions: MutableCollection<String> = mutableListOf()

        for (upstream in upstreams) {
            val update = upstream.fetch(client = client!!, currentVersion = currentVersion, schema = schema)
            if (!update.success || !update.versionUpdate!!) continue

            fetchedUpdates.add(update)
            fetchedVersions.add(update.version!!)
        }

        val highestVersion = VersionComparisonHandler.compareVersionCollection(schema, fetchedVersions)
        for (update in fetchedUpdates) {
            if (update.version!! != highestVersion) continue

            logger.warn(configuration.newUpdateNotification, update.version, update.url)
        }
    }

    private suspend fun InnerUpdater(currentVersion: String, schema: UpdateSchema, upstream: Upstream) {
        val update = upstream.fetch(client = client!!, currentVersion = currentVersion, schema = schema)

        if (!update.success) return
        val versionUpdate = update.versionUpdate!!
        if (!versionUpdate) return

        logger.warn(configuration.newUpdateNotification, update.version, update.url)
    }

}

class UpdaterImpl(configuration: UpdaterConfiguration) : Updater(configuration)

