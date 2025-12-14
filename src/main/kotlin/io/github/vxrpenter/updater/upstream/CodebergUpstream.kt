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

package io.github.vxrpenter.updater.upstream

import io.github.vxrpenter.updater.exceptions.UnsuccessfulVersionRequest
import io.github.vxrpenter.updater.exceptions.VersionTypeMismatch
import io.github.vxrpenter.updater.priority.Priority
import io.github.vxrpenter.updater.priority.Priority.Companion.priority
import io.github.vxrpenter.updater.schema.UpdateSchema
import io.github.vxrpenter.updater.update.DefaultUpdate
import io.github.vxrpenter.updater.update.Update
import io.github.vxrpenter.updater.version.DefaultClassifier
import io.github.vxrpenter.updater.version.DefaultVersion
import io.github.vxrpenter.updater.version.DefaultVersion.Companion.components
import io.github.vxrpenter.updater.version.Version
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException

/**
 * The Codeberg upstream.
 */
class CodebergUpstream (
    /** User that the repository resides under */
    val user: String,
    /** Name of the repository */
    val repo: String,
    override val baseUrl: String? = "https://codeberg.org/api/v1/",
    override val baseUrlEndpoint: String? = "repos/$user/$repo/releases/latest",
    override val releaseBaseUrl: String? = "https://codeberg.org/",
    /**
     * The endpoint where the release should lead to [[baseUrl]+[baseUrlEndpoint]]
     *
     * - **{version}** the current version
     */
    override val releaseBaseUrlEndpoint: String? = "$user/$repo/releases/tag/{version}",
    override val upstreamPriority: Priority = 1.priority
) : Upstream {
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
        val url = "$baseUrl$baseUrlEndpoint"
        val call = client.get(url)
        if (!call.status.value.toString().startsWith("2")) throw UnsuccessfulVersionRequest("Could not correctly commence version request, returned ${call.status.value}")

        try {
            val body = call.body<Release>()

            val value = body.tagName
            val components = components(value, schema)
            val classifier = DefaultClassifier.classifier(value, schema)

            if (classifier != null && classifier.ignored) return null

            return DefaultVersion(value, components, classifier)
        } catch (e: SerializationException) {
            throw UnsuccessfulVersionRequest("Could not correctly commence version request, ${e.message}")
        }
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
    override fun update(version: Version): Update { if (version !is DefaultVersion) throw VersionTypeMismatch("Version type ${version.javaClass} cannot be ${DefaultVersion::class.java}")
        val releaseUrl = "$releaseBaseUrl$releaseBaseUrlEndpoint".replace("{version}", version.value)

        return DefaultUpdate(value = version.value, url = releaseUrl)
    }

    @Serializable
    private data class Release(
        @SerialName("tag_name")
        val tagName: String
    )
}