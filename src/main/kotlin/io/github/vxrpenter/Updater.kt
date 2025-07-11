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

import io.github.vxrpenter.builder.ConfigurationBuilder
import io.github.vxrpenter.data.Update
import io.github.vxrpenter.data.UpdateSchema
import io.github.vxrpenter.data.UpdaterConfiguration
import io.github.vxrpenter.data.Upstream
import io.github.vxrpenter.enum.UpstreamType
import io.github.vxrpenter.handler.GitHubRequestHandler
import io.github.vxrpenter.handler.HangarRequestHandler
import io.github.vxrpenter.handler.ModrinthRequestHandler
import io.github.vxrpenter.handler.SpigotRequestHandler
import okhttp3.OkHttpClient
import kotlin.time.Duration
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory

inline fun Updater(
    sequential: Duration? = null,
    builder: ConfigurationBuilder.() -> Unit
): Updater {
    val internalBuilder = ConfigurationBuilder()
    internalBuilder.builder()

    sequential?.let { internalBuilder.sequential = sequential }

    val conf = internalBuilder.build()
    return UpdaterImpl(conf)
}

sealed class Updater(val configuration: UpdaterConfiguration) {
    private val logger = LoggerFactory.getLogger(Updater::class.java)
    // Defining client
    var client: OkHttpClient? = null

    init {
        client = OkHttpClient.Builder()
            .readTimeout(configuration.readTimeOut.timeout, configuration.readTimeOut.unit)
            .writeTimeout(configuration.writeTimeOut.timeout, configuration.writeTimeOut.unit)
            .build()
    }

    // Default configuration object
    companion object Default : Updater(configuration =  UpdaterConfiguration())

    fun light(currentVersion: String, schema: UpdateSchema, upstream: Upstream) {
        TODO("Purpose is not that clear, will be removed if no usage is found in future development cycles.")
    }

    fun default(currentVersion: String, schema: UpdateSchema, upstream: Upstream) {
        if (configuration.sequential != null) {
            val updatesScope = CoroutineScope(CoroutineExceptionHandler { _, exception ->
                LoggerFactory.getLogger(Updater::class.java).error("An error occurred in the update coroutine", exception)
            })

            InnerUpdater(currentVersion = currentVersion, schema = schema, upstream = upstream)
            Timer().runWithTimer(period = configuration.sequential, coroutineScope = updatesScope) {
                InnerUpdater(currentVersion = currentVersion, schema = schema, upstream = upstream)
            }
        } else {
            InnerUpdater(currentVersion = currentVersion, schema = schema, upstream = upstream)
        }
    }

    private fun InnerUpdater(currentVersion: String, schema: UpdateSchema, upstream: Upstream) {
        val update = returnUpdate(currentVersion = currentVersion, schema = schema, upstream = upstream)

        if (!update.success) return
        val versionUpdate = update.versionUpdate!!
        if (!versionUpdate) return

        val version = update.version!!
        val url = update.url!!

        logger.warn(configuration.newUpdateNotification, version, url)
    }

    fun returnUpdate(currentVersion: String, schema: UpdateSchema, upstream: Upstream): Update {
        return when(upstream.type) {
            UpstreamType.GITHUB -> GitHubRequestHandler().githubRequester(client = client!!, currentVersion = currentVersion, schema = schema, upstream = upstream)
            UpstreamType.MODRINTH -> ModrinthRequestHandler().modrinthRequester(client = client!!, currentVersion = currentVersion, schema = schema, upstream = upstream)
            UpstreamType.HANGAR -> HangarRequestHandler().hangarRequester(client = client!!, currentVersion = currentVersion, schema = schema, upstream = upstream)
            UpstreamType.SPIGOT -> SpigotRequestHandler().spigotRequester(client = client!!, currentVersion = currentVersion, schema = schema, upstream = upstream)
        }
    }
}

class UpdaterImpl(configuration: UpdaterConfiguration) : Updater(configuration)

internal class Timer{
    fun runWithTimer(period: Duration, coroutineScope: CoroutineScope, task: suspend () -> Unit) = runBlocking {
        var taskExecuted = false

        val currentTask: suspend () -> Unit = {
            task()

            taskExecuted = true
        }

        startTimer(period, coroutineScope, currentTask)
        assert(taskExecuted)
    }

    private fun startTimer(period: Duration, coroutineScope: CoroutineScope, task: suspend () -> Unit) {
        coroutineScope.launch {
            launch {
                while (isActive) {
                    task()
                    delay(period)
                }
            }
        }
    }
}