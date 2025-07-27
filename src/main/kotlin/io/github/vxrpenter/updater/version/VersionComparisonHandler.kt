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

package io.github.vxrpenter.updater.version

import io.github.vxrpenter.updater.schema.ClassifierPriority
import io.github.vxrpenter.updater.schema.SchemaClassifier

open class VersionComparisonHandler {
    companion object {
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
    }
}