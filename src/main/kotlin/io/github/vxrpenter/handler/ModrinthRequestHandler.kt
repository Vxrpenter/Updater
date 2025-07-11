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
import io.github.vxrpenter.enum.ModrinthProjectType
import io.github.vxrpenter.handler.data.ModrinthVersionSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.text.replace

class ModrinthRequestHandler {
    private val json = Json { ignoreUnknownKeys = true }

    fun modrinthRequester(client: OkHttpClient, currentVersion: String, schema: UpdateSchema, upstream: Upstream): Update {
        val projectId = upstream.projectId
        require(!projectId.isNullOrBlank()) { "'projectId' for request with ${upstream.type}, cannot be null" }
        require(upstream.modrinthProjectType != null) { "'modrinthProjectType' for request with ${upstream.type}, cannot be null" }

        val url = "https://api.modrinth.com/v2/project/${upstream.projectId}/version"
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return Update(success = false)

            try {
                val serializedBody = json.decodeFromString<List<ModrinthVersionSerializer>>(response.body.string())

                val version = serializedBody.first().versionNumber
                val versionUpdate: Boolean = VersionComparisonHandler().compareVersions(schema = schema, currentVersion = currentVersion, newVersion = version)
                val releaseUrl = "https://modrinth.com/${ModrinthProjectType.findValue(upstream.modrinthProjectType)}/$projectId/version/$version"

                return Update(success = true, versionUpdate = versionUpdate, version = version, url = releaseUrl)
            } catch (e: SerializationException) {
                return Update(success = false)
            }
        }
    }
}