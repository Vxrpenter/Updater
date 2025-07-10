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
import io.github.vxrpenter.data.UpdaterConfiguration
import io.github.vxrpenter.data.UpdateSchema
import io.github.vxrpenter.data.UpdaterConfigurationTimeOut
import io.github.vxrpenter.data.Upstream
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

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
    // Defining client
    var client: OkHttpClient? = null

    init {
        require(configuration.readTimeOut != null) { "'readTimeOut' cannot be null" }
        require(configuration.writeTimeOut != null) { "'writeTimeOut' cannot be null" }

        client = OkHttpClient.Builder()
            .readTimeout(configuration.readTimeOut.timeout, configuration.readTimeOut.unit)
            .writeTimeout(configuration.writeTimeOut.timeout, configuration.writeTimeOut.unit)
            .build()
    }

    // Default configuration object
    companion object Default : Updater(configuration =  UpdaterConfiguration(readTimeOut = UpdaterConfigurationTimeOut(timeout = 30, unit = TimeUnit.SECONDS), writeTimeOut = UpdaterConfigurationTimeOut(timeout = 30, unit = TimeUnit.SECONDS)) )

    fun light(schema: UpdateSchema, upstream: Upstream) {

    }

    fun default(schema: UpdateSchema, upstream: Upstream) {

    }

    // Logic behind GitHub requests
    private fun githubRequester(schema: UpdateSchema, upstream: Upstream) {
        val repository = upstream.repository
        require(!repository.isNullOrBlank()) { "'repository' cannot be null" }
        val tagUrl = upstream.tagUrl

        val url: String = if (tagUrl.isNullOrBlank()) "${repository.replace("https://github.com", "https://api.github.com")}/git/refs/tags" else tagUrl


    }
}

class UpdaterImpl(configuration: UpdaterConfiguration) : Updater(configuration)