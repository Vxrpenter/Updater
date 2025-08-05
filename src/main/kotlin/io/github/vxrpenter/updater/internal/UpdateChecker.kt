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
import io.github.vxrpenter.updater.schema.UpdateSchema
import io.github.vxrpenter.updater.upstream.Upstream
import io.github.vxrpenter.updater.version.Version
import io.ktor.client.HttpClient

class UpdateChecker(val configuration: UpdaterConfiguration, val client: HttpClient) {
    private val logger = KotlinLogging.logger {}

    internal suspend fun checkForUpdate(currentVersion: Version, schema: UpdateSchema, upstream: Upstream) {
        val version = upstream.fetch(client = client, schema = schema)

        version ?: return
        if (currentVersion >= version) return

        val update = upstream.update(version)

        if (configuration.notification.notify) logger.warn {configuration.notification.message
            .replace("{version}", update.value)
            .replace("{url}", update.url)
        }
    }

    internal suspend fun checkMultipleUpdates(currentVersion: String, schema: UpdateSchema, upstreams: Collection<Upstream>) {
        val upstreamVersionPairList = mutableListOf<Pair<Upstream, Version>>()

        for (upstream in upstreams) {
            val version = upstream.fetch(client = client, schema = schema)

            version ?: return
            upstreamVersionPairList.add(Pair(upstream, version))
        }

        val prioritisedVersionPair = upstreamVersionPairList.maxWith(Comparator { versionPair, otherVersionPair -> versionPairComparor(versionPair, otherVersionPair) })
        if (prioritisedVersionPair.first.toVersion(currentVersion, schema) >= prioritisedVersionPair.second) return

        val update = prioritisedVersionPair.first.update(prioritisedVersionPair.second)

        if (configuration.notification.notify) logger.warn {configuration.notification.message
            .replace("{version}", update.value)
            .replace("{url}", update.url)
        }
    }

    private fun versionPairComparor(versionPair: Pair<Upstream, Version>, otherVersionPair: Pair<Upstream, Version>): Int {
        val (version, upstream) = versionPair
        val (otherVersion, otherUpstream) = otherVersionPair

        return when {
            version > otherVersion || upstream > otherUpstream-> 1
            upstream > otherUpstream -> 1
            else -> -1
        }
    }
}