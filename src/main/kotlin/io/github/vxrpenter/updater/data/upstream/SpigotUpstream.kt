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

import io.github.vxrpenter.updater.data.UpdateSchema
import io.github.vxrpenter.updater.data.update.DefaultUpdate
import io.github.vxrpenter.updater.interfaces.Version
import io.github.vxrpenter.updater.data.version.DefaultClassifier
import io.github.vxrpenter.updater.data.version.DefaultVersion
import io.github.vxrpenter.updater.enum.UpstreamPriority
import io.github.vxrpenter.updater.interfaces.Upstream
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

data class SpigotUpstream(
    val projectId: String,
    override val upstreamPriority: UpstreamPriority = UpstreamPriority.NONE
): Upstream {
    override suspend fun fetch(client: HttpClient, schema: UpdateSchema): DefaultVersion? {
        val url = "https://api.spigotmc.org/legacy/update.php?resource=$projectId"
        val call = client.get(url)
        if (call.status.value == 400) return null

        val value = call.bodyAsText()
        val components = components(schema, value)
        val classifier = classifier(schema, value)

        return DefaultVersion(value, components, classifier)
    }

    override fun update(version: Version): DefaultUpdate { version as DefaultVersion
        val releaseUrl = "https://www.spigotmc.org/resources/$projectId/history"

        return DefaultUpdate(version.value, releaseUrl)
    }

    override suspend fun compareVersions(version: Version, other: Version, client: HttpClient, schema: UpdateSchema): Pair<Int, DefaultVersion>? {
        val compare = version.compareTo(other)
        return Pair(compare, version as DefaultVersion)
    }

    override fun classifier(schema: UpdateSchema, value: String): DefaultClassifier? {
        val version = value.replace(schema.prefix, "")

        for (classifier in schema.classifiers) {
            val classifierElement = "${classifier.divider}${classifier.name}"
            if (!version.contains(classifierElement)) continue

            val value = "$classifierElement${version.split(classifierElement).last()}"
            val components = version.split(classifierElement).last().split(classifier.componentDivider)

            return DefaultClassifier(value, classifier.priority, components)
        }

        return null
    }
}