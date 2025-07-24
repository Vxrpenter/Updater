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

package io.github.vxrpenter.updater.data.upstream

import io.github.vxrpenter.updater.data.Update
import io.github.vxrpenter.updater.data.UpdateSchema
import io.github.vxrpenter.updater.handler.VersionComparisonHandler
import io.github.vxrpenter.updater.handler.data.GitHubReleaseSerializer
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.SerializationException

data class GithubUpstream(
    val user: String,
    val repo: String
) : Upstream() {
    override suspend fun fetch(client: HttpClient, currentVersion: String, schema: UpdateSchema): Update {
        val project = "$user/$repo"
        val url = "https://api.github.com/repos/${project}/releases"
        val call = client.get(url)
        if (call.status.value == 400) return Update(success = false)

        try {
            val body = call.body<List<GitHubReleaseSerializer>>()

            val version = body.first().tagName
            val versionUpdate: Boolean = VersionComparisonHandler.Default.compareVersions(schema = schema, currentVersion = currentVersion, newVersion = version)
            val releaseUrl = "https://github.com/$project/releases/tag/${version}"

            return Update(success = true, versionUpdate = versionUpdate, version = version, url = releaseUrl)
        } catch (_: SerializationException) {
            return Update(success = false)
        }
    }
}
