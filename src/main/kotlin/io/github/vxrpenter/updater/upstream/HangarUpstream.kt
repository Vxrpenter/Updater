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

package io.github.vxrpenter.updater.upstream

import io.github.vxrpenter.updater.exceptions.ClassifierTypeMismatch
import io.github.vxrpenter.updater.update.DefaultUpdate
import io.github.vxrpenter.updater.version.DefaultClassifier
import io.github.vxrpenter.updater.version.DefaultVersion
import io.github.vxrpenter.updater.exceptions.VersionTypeMismatch
import io.github.vxrpenter.updater.schema.HangarSchemaClassifier
import io.github.vxrpenter.updater.version.VersionComparisonHandler
import io.github.vxrpenter.updater.schema.SchemaClassifier
import io.github.vxrpenter.updater.update.Update
import io.github.vxrpenter.updater.schema.UpdateSchema
import io.github.vxrpenter.updater.version.Version
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

private val versions = mutableListOf<Pair<String, SchemaClassifier>>()

/**
 * The Hangar upstream.
 */
data class HangarUpstream(
    /**
     * Id of the project
     */
    val projectId: String,
    override val upstreamPriority: UpstreamPriority = UpstreamPriority.NONE
) : Upstream {
    /**
     * Fetches a version object from the upstream.
     *
     * @param client defines the [HttpClient] used for the fetching
     * @param schema defines the version deserialization
     *
     * @return the fetched [DefaultVersion]
     * @throws ClassifierTypeMismatch when [SchemaClassifier] in [schema] is not [HangarSchemaClassifier]
     */
    override suspend fun fetch(client: HttpClient, schema: UpdateSchema): DefaultVersion? {
        for (classifier in schema.classifiers) { if (classifier !is HangarSchemaClassifier) throw ClassifierTypeMismatch("Classifier type ${classifier.javaClass} cannot be ${HangarSchemaClassifier::class.java}")
            val url = "https://hangar.papermc.io/api/v1/projects/${projectId}/latest?channel=${classifier.channel}"
            val call = client.get(url)
            if (call.status.value == 400) return null

            versions.add(Pair(call.bodyAsText(), classifier))
        }

        val value = VersionComparisonHandler.returnPrioritisedVersion(list = versions)
        val components = components(value, schema)
        val classifier = classifier(value, schema)

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
        return DefaultVersion(version, components(version, schema), classifier(version, schema))
    }

    /**
     * Returns an [Update] from a [DefaultVersion].
     *
     * @param version the version
     * @return the [Update]
     * @throws VersionTypeMismatch when [version] is not [DefaultVersion]
     */
    override fun update(version: Version): DefaultUpdate { if (version !is DefaultVersion) throw VersionTypeMismatch("Version type ${version.javaClass} cannot be ${DefaultVersion::class.java}")
        val version = VersionComparisonHandler.returnPrioritisedVersion(list = versions)
        val releaseUrl = "https://hangar.papermc.io/${projectId}/versions/$version"

        return DefaultUpdate(version, releaseUrl)
    }

    /**
     * Returns a [DefaultClassifier] from the given version.
     *
     * @param value complete version
     * @param schema defines the version deserialization
     * @return the [DefaultClassifier]
     */
    override fun classifier(value: String, schema: UpdateSchema): DefaultClassifier? {
        val version = value.replace(schema.prefix, "")

        for (classifier in schema.classifiers) {
            val classifierElement = "${classifier.divider}${classifier.value}"
            if (!version.contains(classifierElement)) continue

            val value = "$classifierElement${version.split(classifierElement).last()}"
            val components = version.split(classifierElement).last().split(classifier.componentDivider)

            return DefaultClassifier(value, classifier.priority, components)
        }

        return null
    }
}