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

package io.github.vxrpenter.data.upstream

import io.github.vxrpenter.data.SchemaClassifier
import io.github.vxrpenter.data.Update
import io.github.vxrpenter.data.UpdateSchema
import io.github.vxrpenter.handler.VersionComparisonHandler
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

data class HangarUpstream(
    val projectId: String
) : Upstream() {
    override suspend fun fetch(client: HttpClient, currentVersion: String, schema: UpdateSchema): Update {
        val versions: MutableList<Pair<String, SchemaClassifier>> = mutableListOf()
        for (classifier in schema.classifiers) {
            val url = "https://hangar.papermc.io/api/v1/projects/${projectId}/latest?channel=${classifier.channel}"
            val call = client.get(url)
            if (call.status.value == 400) return Update(success = false)

            versions.add(Pair(call.bodyAsText(), classifier))
        }

        val version = VersionComparisonHandler.returnPrioritisedVersion(list = versions)
        val versionUpdate: Boolean = VersionComparisonHandler.compareVersions(schema = schema, currentVersion = currentVersion, newVersion = version)
        val releaseUrl = "https://hangar.papermc.io/${projectId}/versions/$version"

        return Update(success = true, versionUpdate = versionUpdate, version = version, url = releaseUrl)
    }
}