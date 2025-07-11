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

package io.github.vxrpenter.data

import java.util.concurrent.TimeUnit
import kotlin.time.Duration

data class UpdaterConfiguration(
    val sequential: Duration? = null,
    val readTimeOut: UpdaterConfigurationTimeOut = UpdaterConfigurationTimeOut(timeout = 30, unit = TimeUnit.SECONDS),
    val writeTimeOut: UpdaterConfigurationTimeOut = UpdaterConfigurationTimeOut(timeout = 30, unit = TimeUnit.SECONDS),
    val newUpdateNotification: String = "New update has been found. Version {} can be downloaded from {}"
)

data class UpdaterConfigurationTimeOut(
    val timeout: Long,
    val unit: TimeUnit
)