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

package io.github.vxrpenter.updater.handler

import io.github.vxrpenter.updater.data.SchemaClassifier
import io.github.vxrpenter.updater.data.UpdateSchema
import io.github.vxrpenter.updater.enum.ClassifierPriority
import io.github.vxrpenter.updater.exceptions.VersionSizeMisMatchException

open class VersionComparisonHandler {
    companion object Default: VersionComparisonHandler()

    fun compareVersions(schema: UpdateSchema, currentVersion: String, newVersion: String): Boolean {
        val versionBuilder = VersionSplitBuilder(schema = schema, list = listOf(currentVersion, newVersion))

        val splittetCurrentVersion = versionBuilder.splittetVersionList[0]
        val splittetNewVersion = versionBuilder.splittetVersionList[1]
        val versionClassifierMap = versionBuilder.versionClassifierMap

        if (splittetCurrentVersion.size != splittetNewVersion.size) throw VersionSizeMisMatchException(
            "Could not compare version strings: '$currentVersion' and '$newVersion'",
            Throwable("Size of split version strings does not match up, cannot compare currentVersion (${splittetCurrentVersion.size}) to newVersion (${splittetNewVersion.size})")
        )

        val currentVersionMap: HashMap<Int, Boolean> = hashMapOf()
        val newVersionMap: HashMap<Int, Boolean> = hashMapOf()
        for ((index, version) in splittetCurrentVersion.withIndex()) {
            currentVersionMap[index] = version >= splittetNewVersion[index]
            newVersionMap[index] = version < splittetNewVersion[index]
        }

        currentVersionMap.forEach { currentVersion ->
            val currentVersionDifferentiation = currentVersion.value
            val newVersionDifferentiation = newVersionMap[currentVersion.key]!!

            val currentVersionPriority = ClassifierPriority.Companion.findValue(versionClassifierMap[splittetCurrentVersion]!!.priority)!!
            val newVersionPriority = ClassifierPriority.Companion.findValue(versionClassifierMap[splittetNewVersion]!!.priority)!!

            if (!currentVersionDifferentiation && newVersionDifferentiation && currentVersionPriority <= newVersionPriority) return true
        }

        return false
    }

    fun returnPrioritisedVersion(list: Collection<Pair<String, SchemaClassifier>>): String {
        var prioritizedVersion = ""
        var prioritisedClassifier: SchemaClassifier? = null

        for (pair in list) {
            if (prioritizedVersion.isBlank()) prioritizedVersion = pair.first
            if (prioritisedClassifier == null) prioritisedClassifier = pair.second

            val currentPrioritiedPriority = ClassifierPriority.Companion.findValue(prioritisedClassifier.priority)!!
            val priority = ClassifierPriority.Companion.findValue(pair.second.priority)!!

            if (currentPrioritiedPriority < priority) {
                prioritizedVersion = pair.first
                prioritisedClassifier = pair.second
            }
        }

        return prioritizedVersion
    }

    fun compareVersionCollection(schema: UpdateSchema, versions: Collection<String>): String {
        var prioritizedVersion = ""

        for (version in versions) {
            if (prioritizedVersion.isBlank()) {
                prioritizedVersion = version
                continue
            }

            if (compareVersions(schema, prioritizedVersion, version)) prioritizedVersion = version
        }

        return prioritizedVersion
    }

    internal class VersionSplitBuilder(schema: UpdateSchema, list: Collection<String>, makeDividerCheck: Boolean = true) {
        val splittetVersionList = mutableListOf<List<String>>()
        val versionClassifierMap: HashMap<List<String>, SchemaClassifier> = hashMapOf()

        init {
            list.forEach { version ->
                // Split version using the divider
                splittetVersionList.add((version.replace(schema.prefix, "").split(schema.divider)))
            }

            for (classifier in schema.classifiers) {
                val classifierElement = "${classifier.divider}${classifier.name}"

                for ((index, version) in list.withIndex()) {
                    if (classifier.divider == schema.divider && makeDividerCheck) splittetVersionList[index] = version.replace(classifierElement, "").split(schema.divider)
                    versionClassifierMap[splittetVersionList[index]] = classifier
                }
            }
        }
    }
}