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

package io.github.vxrpenter.updater.builder

import io.github.vxrpenter.updater.data.UpdaterConfiguration
import io.github.vxrpenter.updater.data.UpdaterConfigurationTimeOut
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

class ConfigurationBuilder {
    var periodic: Duration? = null
    var readTimeOut: UpdaterConfigurationTimeOut = UpdaterConfiguration().readTimeOut
    var writeTimeOut: UpdaterConfigurationTimeOut? = UpdaterConfiguration().writeTimeOut
    var newUpdateNotification: String = UpdaterConfiguration().newUpdateNotification

    fun readTimeout(
        timeout: Long? = null,
        unit: TimeUnit? = null,
        build: InlineUpdaterConfigurationTimeOut.() -> Unit
    ) { readTimeOut = timeoutProcessing(timeout, unit, build) }

    fun writeTimeout(
        timeout: Long? = null,
        unit: TimeUnit? = null,
        build: InlineUpdaterConfigurationTimeOut.() -> Unit
    ) { writeTimeOut = timeoutProcessing(timeout, unit, build) }

    data class InlineUpdaterConfigurationTimeOut(
        var timeout: Long? = null,
        var unit: TimeUnit? = null
    )

    fun build(): UpdaterConfiguration {
        require(!newUpdateNotification.isBlank()) { "'newUpdateNotification' cannot be empty" }

        return UpdaterConfiguration(periodic)
    }

    private fun timeoutProcessing(timeout: Long? = null, unit: TimeUnit? = null, build: InlineUpdaterConfigurationTimeOut.() -> Unit): UpdaterConfigurationTimeOut {
        val timeout = InlineUpdaterConfigurationTimeOut(timeout, unit).apply(build)
        requireNotNull(timeout.timeout != null)
        requireNotNull(timeout.unit != null)
        return UpdaterConfigurationTimeOut(timeout.timeout!!, timeout.unit!!)
    }
}