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

package io.github.vxrpenter.updater.handler

import io.github.vxrpenter.updater.data.SchemaClassifier
import io.github.vxrpenter.updater.data.UpdateSchema
import io.github.vxrpenter.updater.enum.ClassifierPriority

open class VersionComparisonHandler {
    companion object {
        fun returnPrioritisedVersion(list: Collection<Pair<String, SchemaClassifier>>): String {
            var prioritizedVersion = ""
            var prioritisedClassifier: SchemaClassifier? = null

            for (pair in list) {
                if (prioritizedVersion.isBlank()) prioritizedVersion = pair.first
                if (prioritisedClassifier == null) prioritisedClassifier = pair.second

                val currentPrioritiedPriority = ClassifierPriority.findValue(prioritisedClassifier.priority)!!
                val priority = ClassifierPriority.findValue(pair.second.priority)!!

                if (currentPrioritiedPriority < priority) {
                    prioritizedVersion = pair.first
                    prioritisedClassifier = pair.second
                }
            }

            return prioritizedVersion
        }
    }
}