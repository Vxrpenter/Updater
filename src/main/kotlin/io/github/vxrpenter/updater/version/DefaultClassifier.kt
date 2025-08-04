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

@file:Suppress("unused", "FunctionName")

package io.github.vxrpenter.updater.version

import io.github.vxrpenter.updater.exceptions.ClassifierTypeMismatch
import io.github.vxrpenter.updater.exceptions.VersionSizeMismatch
import io.github.vxrpenter.updater.priority.Priority
import io.github.vxrpenter.updater.schema.UpdateSchema

/**
 * The default classifier
 */
data class DefaultClassifier(
    override val value: String,
    /**
     * Priority of the classifier
     */
    val priority: Priority,
    /**
     * Collection of the version components
     */
    val components: Collection<String>
) : Classifier {
    companion object {
        /**
         * Returns a [DefaultClassifier] from the given version.
         *
         * @param value complete version
         * @param schema defines the version deserialization
         * @return the [DefaultClassifier]
         */
        fun classifier(value: String, schema: UpdateSchema): DefaultClassifier? {
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

    /**
     * Compares this object with the specified object for order. Returns zero if this object is equal
     * to the specified [other] object, a negative number if it's less than [other], or a positive number
     * if it's greater than [other].
     *
     * @throws ClassifierTypeMismatch when [other] is not [DefaultClassifier]
     * @throws VersionSizeMismatch when the component sizes don't match
     */
    override fun compareTo(other: Classifier): Int { if (other !is DefaultClassifier) throw ClassifierTypeMismatch("Version type ${other.javaClass} cannot be ${DefaultClassifier::class.java}")
        if (components.size != other.components.size) throw VersionSizeMismatch("Size of classifier components are not equal")
        if (components.isEmpty()) return priority.compareTo(other.priority)

        components.zip(other.components).forEach { (subVersion, otherSubVersion) ->
            if (subVersion != otherSubVersion) return subVersion.compareTo(otherSubVersion)
        }

        return 0
    }
}
