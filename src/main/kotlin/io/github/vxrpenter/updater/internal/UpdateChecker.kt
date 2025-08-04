/*
 * Copyright (c) 2025 Vxrpenter and the Updater contributors
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

package io.github.vxrpenter.updater.internal

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.vxrpenter.updater.configuration.UpdaterConfiguration
import io.github.vxrpenter.updater.exceptions.UnsuccessfulVersionFetch
import io.github.vxrpenter.updater.schema.UpdateSchema
import io.github.vxrpenter.updater.upstream.Upstream
import io.github.vxrpenter.updater.version.Version
import io.ktor.client.HttpClient

class UpdateChecker(val configuration: UpdaterConfiguration, val client: HttpClient) {
    private val logger = KotlinLogging.logger {}

    internal suspend fun checkForUpdate(currentVersion: Version, schema: UpdateSchema, upstream: Upstream) {
        val version = upstream.fetch(client = client, schema = schema)

        version ?: throw UnsuccessfulVersionFetch("Could not fetch version from upstream")
        if (currentVersion >= version) return

        val update = upstream.update(version)

        if (configuration.notification.notify) logger.warn {configuration.notification.notification
            .replace("{version}", update.value)
            .replace("{url}", update.url)
        }
    }
}