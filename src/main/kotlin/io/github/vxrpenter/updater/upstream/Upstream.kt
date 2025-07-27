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

package io.github.vxrpenter.updater.upstream

import io.github.vxrpenter.updater.enum.UpstreamPriority
import io.github.vxrpenter.updater.update.Update
import io.github.vxrpenter.updater.schema.UpdateSchema
import io.github.vxrpenter.updater.version.Classifier
import io.github.vxrpenter.updater.version.Version
import io.ktor.client.HttpClient

/**
 * The Upstream interface defines a remote that stores version information (and possible version files)
 * that allows
 * the fetching of this information through an api.
 */
interface Upstream {
    /**
     * Priority is that used when comparing versions from multiple upstreams.
     */
    val upstreamPriority: UpstreamPriority

    /**
     * Fetches a version object from the upstream.
     *
     * @param client defines the [io.ktor.client.HttpClient] used for the fetching
     * @param schema defines the version deserialization
     *
     * @return the fetched [io.github.vxrpenter.updater.version.Version]
     */
    suspend fun fetch(client: HttpClient, schema: UpdateSchema): Version?

    /**
     * Converts a version string into a [Version].
     *
     * @param version complete version
     * @param schema defines the version deserialization
     *
     * @return the [Version]
     */
    fun toVersion(version: String, schema: UpdateSchema): Version

    /**
     * Returns an [Update] from a [Version].
     *
     * @param version the version
     * @return the [Update]
     */
    fun update(version: Version): Update

    /**
     * Returns a collection of version components from the given version.
     *
     * @param value complete version
     * @param schema defines the version deserialization
     * @return the component collection
     */
    fun components(value: String, schema: UpdateSchema): Collection<String> {
        val version = value.replace(schema.prefix, "")
        var preSplit = version

        for (classifier in schema.classifiers) {
            val classifierElement = "${classifier.divider}${classifier.value}"
            if (!version.contains(classifierElement)) continue

            preSplit = version.split(classifierElement).first()
        }

        return preSplit.split(schema.divider)
    }

    /**
     * Returns a [io.github.vxrpenter.updater.version.Classifier] from the given version.
     *
     * @param value complete version
     * @param schema defines the version deserialization
     * @return the [io.github.vxrpenter.updater.version.Classifier]
     */
    fun classifier(value: String, schema: UpdateSchema): Classifier?
}