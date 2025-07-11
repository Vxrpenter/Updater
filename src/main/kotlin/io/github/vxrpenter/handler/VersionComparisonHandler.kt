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

import io.github.vxrpenter.data.SchemaGroup
import io.github.vxrpenter.data.UpdateSchema
import io.github.vxrpenter.enum.GroupPriority
import io.github.vxrpenter.exceptions.VersionSizeMisMatchException

class VersionComparisonHandler {
    fun compareVersions(schema: UpdateSchema, currentVersion: String, newVersion: String): Boolean {
        var splittetCurrentVersion = currentVersion.split(schema.divider)
        var splittetNewVersion = newVersion.split(schema.divider)

        if (splittetCurrentVersion.size != splittetNewVersion.size) throw VersionSizeMisMatchException("Could not compare version strings: '$currentVersion' and '$newVersion'",
            Throwable("Size of split version strings does not match up, cannot compare currentVersion (${splittetCurrentVersion.size}) to newVersion (${splittetNewVersion.size})"))


        val versionGroupMap: HashMap<List<String>, SchemaGroup> = hashMapOf()

        for (group in schema.groups) {
            val groupElement = "${group.divider}${group.name}"
            if (currentVersion.contains(groupElement)) {
                if (group.divider == schema.divider) splittetCurrentVersion = currentVersion.replace(groupElement, "").split(schema.divider)
                versionGroupMap[splittetCurrentVersion] = group
            }
            if (newVersion.contains(groupElement)) {
                if (group.divider == schema.divider) splittetNewVersion = newVersion.replace(groupElement, "").split(schema.divider)
                versionGroupMap[splittetNewVersion] = group
            }

        }

        var count = 0
        val currentVersionList: HashMap<Int, Boolean> = hashMapOf()
        val newVersionList: HashMap<Int, Boolean> = hashMapOf()
        for (version in splittetCurrentVersion) {
            currentVersionList[count] = version >= splittetNewVersion[count]
            newVersionList[count] = version < splittetNewVersion[count]
            count = count+1
        }

        currentVersionList.forEach { currentVersion ->
            val currentVersionDifferentiation = currentVersion.value
            val newVersionDifferentiation = newVersionList[currentVersion.key]!!

            val currentVersionPriority = GroupPriority.findValue(versionGroupMap[splittetCurrentVersion]!!.priority)!!
            val newVersionPriority = GroupPriority.findValue(versionGroupMap[splittetNewVersion]!!.priority)!!

            if (!currentVersionDifferentiation && newVersionDifferentiation && currentVersionPriority < newVersionPriority) return true
        }
        return false
    }

    fun returnPrioritisedVersion(schema: UpdateSchema, list: List<String>): String {
        return "false"
    }
}