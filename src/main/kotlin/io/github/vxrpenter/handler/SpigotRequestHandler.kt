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

class SpigotRequestHandler {
    fun spigotRequester(client: OkHttpClient, currentVersion: String, schema: UpdateSchema, upstream: Upstream): Update {
        val projectId = upstream.projectId
        require(!projectId.isNullOrBlank()) { "'projectId' for request with ${upstream.type}, cannot be null" }

        val url = "https://api.spigotmc.org/legacy/update.php?resource=$projectId"
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return Update(success = false)

            val version = response.body.string()
            val versionUpdate: Boolean = VersionComparisonHandler().compareVersions(schema = schema, currentVersion = currentVersion, newVersion = version)
            val releaseUrl = "https://www.spigotmc.org/resources/$projectId/history"

            return Update(success = true, versionUpdate = versionUpdate, version = version, url = releaseUrl)
        }
    }
}