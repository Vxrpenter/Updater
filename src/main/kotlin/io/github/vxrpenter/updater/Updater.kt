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

import io.github.vxrpenter.updater.annotations.ExperimentalScheduler
import io.github.vxrpenter.updater.builder.ConfigurationBuilder
import io.github.vxrpenter.updater.data.UpdateSchema
import io.github.vxrpenter.updater.data.UpdaterConfiguration
import io.github.vxrpenter.updater.exceptions.UnsuccessfulVersionFetch
import io.github.vxrpenter.updater.interfaces.UpdaterInterface
import io.github.vxrpenter.updater.interfaces.UpstreamInterface
import io.github.vxrpenter.updater.interfaces.VersionInterface
import io.ktor.client.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import org.slf4j.LoggerFactory

sealed class Updater(override var configuration: UpdaterConfiguration) : UpdaterInterface {
    private val logger = LoggerFactory.getLogger(Updater::class.java)
    private val updatesScope = CoroutineScope(CoroutineExceptionHandler { _, exception -> logger.error("An error occurred in the update coroutine", exception) })
    override var client: HttpClient = createClient()

    // Default configuration object
    companion object Default : Updater(configuration = UpdaterConfiguration())

    @OptIn(ExperimentalScheduler::class)
    override suspend fun default(currentVersion: String, schema: UpdateSchema, upstream: UpstreamInterface, builder: (ConfigurationBuilder.() -> Unit)?) {
        if (builder != null) runBuilder(builder)

        // Logic
        if (configuration.periodic != null) {
            Timer.schedule(period = configuration.periodic!!, coroutineScope = updatesScope) {
                innerUpdater(currentVersion = upstream.toVersion(currentVersion, schema), schema = schema, upstream = upstream)
            }
        } else {
            innerUpdater(currentVersion = upstream.toVersion(currentVersion, schema), schema = schema, upstream = upstream)
        }
    }

    private suspend fun innerUpdater(currentVersion: VersionInterface, schema: UpdateSchema, upstream: UpstreamInterface) {
        val version = upstream.fetch(client = client, schema = schema)

        version ?: throw UnsuccessfulVersionFetch("Could not fetch version from upstream", Throwable("Either upstream not available or serializer out of date"))
        if (currentVersion <= version) return

        val update = upstream.update(version)

        logger.warn(configuration.newUpdateNotification, update.value, update.url)
    }
}