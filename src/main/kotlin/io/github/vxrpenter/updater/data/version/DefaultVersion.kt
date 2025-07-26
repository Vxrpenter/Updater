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

package io.github.vxrpenter.updater.data.version

import io.github.vxrpenter.updater.interfaces.VersionInterface

data class DefaultVersion(
    override val value: String,
    val components: Collection<String>,
    val classifier: DefaultClassifier?
) : VersionInterface {
    override fun compareTo(other: VersionInterface): Int { other as DefaultVersion
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