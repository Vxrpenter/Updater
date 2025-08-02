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

@file:Suppress("unused", "FunctionName")

package io.github.vxrpenter.updater.upstream

import io.github.vxrpenter.updater.exceptions.UnsuccessfulVersionRequest
import io.github.vxrpenter.updater.update.DefaultUpdate
import io.github.vxrpenter.updater.version.DefaultClassifier
import io.github.vxrpenter.updater.version.DefaultVersion
import io.github.vxrpenter.updater.exceptions.VersionTypeMismatch
import io.github.vxrpenter.updater.update.Update
import io.github.vxrpenter.updater.schema.UpdateSchema
import io.github.vxrpenter.updater.version.DefaultVersion.Companion.components
import io.github.vxrpenter.updater.version.Version
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

/**
 * The Spigot upstream.
 */
data class SpigotUpstream(
    /**
     * Id of the project
     */
    val projectId: String,
    override val upstreamPriority: UpstreamPriority = UpstreamPriority.NONE
): Upstream {
    /**
     * Fetches a version object from the upstream.
     *
     * @param client defines the [HttpClient] used for the fetching
     * @param schema defines the version deserialization
     *
     * @return the fetched [DefaultVersion]
     * @throws UnsuccessfulVersionRequest when version request fails
     */
    override suspend fun fetch(client: HttpClient, schema: UpdateSchema): DefaultVersion? {
        val url = "https://api.spigotmc.org/legacy/update.php?resource=$projectId"
        val call = client.get(url)
        if (!call.status.value.toString().startsWith("2")) throw UnsuccessfulVersionRequest("Could not correctly commence version request, returned ${call.status.value}")

        val value = call.bodyAsText()
        val components = DefaultVersion.components(value, schema)
        val classifier = DefaultClassifier.classifier(value, schema)

        return DefaultVersion(value, components, classifier)
    }

    /**
     * Converts a version string into a [DefaultVersion].
     *
     * @param version complete version
     * @param schema defines the version deserialization
     *
     * @return the [DefaultVersion]
     */
    override fun toVersion(version: String, schema: UpdateSchema): DefaultVersion {
        return DefaultVersion(version, components(version, schema), DefaultClassifier.classifier(version, schema))
    }

    /**
     * Returns an [Update] from a [DefaultVersion].
     *
     * @param version the version
     * @return the [Update]
     * @throws VersionTypeMismatch when [version] is not [DefaultVersion]
     */
    override fun update(version: Version): DefaultUpdate { if (version !is DefaultVersion) throw VersionTypeMismatch("Version type ${version.javaClass} cannot be ${DefaultVersion::class.java}")
        val releaseUrl = "https://www.spigotmc.org/resources/$projectId/history"

        return DefaultUpdate(version.value, releaseUrl)
    }
}