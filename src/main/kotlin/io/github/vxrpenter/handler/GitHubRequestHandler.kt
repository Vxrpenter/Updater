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
import io.github.vxrpenter.handler.data.GitHubReleaseSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request

class GitHubRequestHandler {
    private val json = Json { ignoreUnknownKeys = true }

    fun githubRequester(client: OkHttpClient, currentVersion: String, schema: UpdateSchema, upstream: Upstream): Update {
        val projectId = upstream.projectId
        require(!projectId.isNullOrBlank()) { "'projectId' for request with ${upstream.type}, cannot be null" }

        val url = "https://api.github.com/repos/${projectId}/releases"
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return Update(success = false)

            try {
                val serializedBody = json.decodeFromString<List<GitHubReleaseSerializer>>(response.body.string())

                val version = serializedBody.first().tagName
                val versionUpdate: Boolean = VersionComparisonHandler().compareVersions(schema = schema, currentVersion = currentVersion, newVersion = version)
                val releaseUrl = "https://github.com/$projectId/releases/tag/${version}"

                return Update(success = true, versionUpdate = versionUpdate, version = version, url = releaseUrl)
            } catch (e: SerializationException) {
                return Update(success = false)
            }
        }
    }
}