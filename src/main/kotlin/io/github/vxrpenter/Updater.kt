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
import io.github.vxrpenter.data.Configuration
import io.github.vxrpenter.data.UpdateSchema
import io.github.vxrpenter.data.Upstream
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

sealed class Updater(configuration: Configuration) {

    companion object Default : Updater(configuration =  Configuration(sequential = null) )

    fun light(schema: UpdateSchema, upstream: Upstream) {

    }

    fun default(schema: UpdateSchema, upstream: Upstream) {

    }
}

class UpdaterImpl(configuration: Configuration) : Updater(configuration)