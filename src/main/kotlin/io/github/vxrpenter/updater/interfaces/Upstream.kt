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

package io.github.vxrpenter.updater.interfaces

import io.github.vxrpenter.updater.data.UpdateSchema
import io.github.vxrpenter.updater.enum.UpstreamPriority
import io.ktor.client.HttpClient

interface Upstream {
    val upstreamPriority: UpstreamPriority

    suspend fun fetch(client: HttpClient, schema: UpdateSchema): Version?

    suspend fun compareVersions(version: Version, other: Version, client: HttpClient, schema: UpdateSchema): Pair<Int, Version>?

    fun update(version: Version): Update

    fun components(schema: UpdateSchema, value: String): Collection<String> {
        val version = value.replace(schema.prefix, "")

        for (classifier in schema.classifiers) {
            val classifierElement = "${classifier.divider}${classifier.name}"
            if (!version.contains(classifierElement)) continue

            val preSplit = version.split(classifierElement, "").first()
            return preSplit.split(schema.divider)
        }

        return emptyList()
    }

    fun classifier(schema: UpdateSchema, value: String): Classifier?
}