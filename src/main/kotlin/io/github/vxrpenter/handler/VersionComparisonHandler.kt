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

import io.github.vxrpenter.annotations.Internal
import io.github.vxrpenter.data.SchemaGroup
import io.github.vxrpenter.data.UpdateSchema
import io.github.vxrpenter.enum.GroupPriority
import io.github.vxrpenter.exceptions.VersionSizeMisMatchException
import kotlin.collections.set

class VersionComparisonHandler {
    @OptIn(Internal::class)
    fun compareVersions(schema: UpdateSchema, currentVersion: String, newVersion: String): Boolean {
        val versionBuilder = VersionSplitBuilder(schema = schema, list = listOf(currentVersion, newVersion))

        val splittetCurrentVersion = versionBuilder.splittetVersionList[0]
        val splittetNewVersion = versionBuilder.splittetVersionList[1]
        val versionGroupMap = versionBuilder.versionGroupMap

        if (splittetCurrentVersion.size != splittetNewVersion.size) throw VersionSizeMisMatchException("Could not compare version strings: '$currentVersion' and '$newVersion'",
            Throwable("Size of split version strings does not match up, cannot compare currentVersion (${splittetCurrentVersion.size}) to newVersion (${splittetNewVersion.size})"))

        val currentVersionMap: HashMap<Int, Boolean> = hashMapOf()
        val newVersionMap: HashMap<Int, Boolean> = hashMapOf()
        for ((index, version) in splittetCurrentVersion.withIndex()) {
            currentVersionMap[index] = version >= splittetNewVersion[index]
            newVersionMap[index] = version < splittetNewVersion[index]
        }

        currentVersionMap.forEach { currentVersion ->
            val currentVersionDifferentiation = currentVersion.value
            val newVersionDifferentiation = newVersionMap[currentVersion.key]!!

            val currentVersionPriority = GroupPriority.findValue(versionGroupMap[splittetCurrentVersion]!!.priority)!!
            val newVersionPriority = GroupPriority.findValue(versionGroupMap[splittetNewVersion]!!.priority)!!

            if (!currentVersionDifferentiation && newVersionDifferentiation && currentVersionPriority <= newVersionPriority) return true
        }

        return false
    }

    @OptIn(Internal::class)
    fun returnPrioritisedVersion(list: List<Pair<String, SchemaGroup>>): String {
        var currentPrioritizedVersion = ""
        var currenPrioritizedGroup: SchemaGroup? = null

        for (pair in list) {
            if (currentPrioritizedVersion.isBlank()) currentPrioritizedVersion = pair.first
            if (currenPrioritizedGroup == null) currenPrioritizedGroup = pair.second

            val currentPrioritiedPriority = GroupPriority.findValue(currenPrioritizedGroup.priority)!!
            val priority = GroupPriority.findValue(pair.second.priority)!!

            if (currentPrioritiedPriority < priority) {
                currentPrioritizedVersion = pair.first
                currenPrioritizedGroup = pair.second
            }
        }

        return currentPrioritizedVersion
    }

    @Internal
    internal class VersionSplitBuilder(schema: UpdateSchema, list: List<String>, makeDividerCheck: Boolean = true) {
        val splittetVersionList = mutableListOf<List<String>>()
        val versionGroupMap: HashMap<List<String>, SchemaGroup> = hashMapOf()

        init {
            list.forEach { version -> splittetVersionList.add((version.split(schema.divider))) }

            for (group in schema.groups) {
                val groupElement = "${group.divider}${group.name}"

                for ((index, version) in list.withIndex()) {
                    if (group.divider == schema.divider && makeDividerCheck) splittetVersionList[index] = version.replace(groupElement, "").split(schema.divider)
                    versionGroupMap[splittetVersionList[index]] = group
                }
            }
        }
    }
}