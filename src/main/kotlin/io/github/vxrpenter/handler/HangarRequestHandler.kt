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

package io.github.vxrpenter.handler

import io.github.vxrpenter.data.Update
import io.github.vxrpenter.data.UpdateSchema
import io.github.vxrpenter.data.Upstream
import okhttp3.OkHttpClient
import okhttp3.Request

class HangarRequestHandler {
    fun hangarRequester(client: OkHttpClient, currentVersion: String, schema: UpdateSchema, upstream: Upstream): Update {
        val projectId = upstream.projectId
        require(!projectId.isNullOrBlank()) { "'projectId' for request with ${upstream.type}, cannot be null" }

        val versions: MutableList<String> = mutableListOf()
        for (group in schema.groups) {
            require(!group.channel.isNullOrBlank()) { "'channel' for request with ${upstream.type}, cannot be null" }

            val url = "https://hangar.papermc.io/api/v1/projects/${projectId}/latest?channel=${group.channel}"
            val request = Request.Builder().url(url).build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return Update(success = false)

                val version = response.body.string()
                versions.add(version)
            }
        }

        val version = VersionComparisonHandler().returnPrioritisedVersion(schema = schema, list = versions)
        val versionUpdate: Boolean = VersionComparisonHandler().compareVersions(schema = schema, currentVersion = currentVersion, newVersion = version)
        val releaseUrl = "https://hangar.papermc.io/${projectId}/versions/$version"

        return Update(success = true, versionUpdate = versionUpdate, version = version, url = releaseUrl)
    }
}