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

import io.github.vxrpenter.updater.exceptions.VersionSizeMismatch

/**
 * The default version
 */
data class DefaultVersion(
    override val value: String,
    /**
     * Collection of the version components
     */
    val components: Collection<String>,
    /**
     * Classifier contained in the version
     */
    val classifier: DefaultClassifier?
) : Version {
    /**
     * Compares this object with the specified object for order. Returns zero if this object is equal
     * to the specified [other] object, a negative number if it's less than [other], or a positive number
     * if it's greater than [other].
     *
     * @throws VersionSizeMismatch when the component sizes don't match
     */
    override fun compareTo(other: Version): Int { other as DefaultVersion
        if (components.size != other.components.size) throw VersionSizeMismatch("Size of version components are not equal")
        components.zip(other.components).forEach { (subVersion, otherSubVersion) ->
            if (subVersion != otherSubVersion) return subVersion.compareTo(otherSubVersion)
        }

        if (classifier != other.classifier) return when {
            classifier == null -> 1
            other.classifier == null -> -1
            else -> return classifier.compareTo(other.classifier)
        }

        return 0
    }
}