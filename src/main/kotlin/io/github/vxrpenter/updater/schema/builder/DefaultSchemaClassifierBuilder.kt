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

package io.github.vxrpenter.updater.schema.builder

import io.github.vxrpenter.updater.priority.Priority
import io.github.vxrpenter.updater.schema.DefaultSchemaClassifier

class DefaultSchemaClassifierBuilder : SchemaClassifierBuilder {
    override var value: String? = null
    override var divider: String? = null
    override var priority: Priority? = null
    override var componentDivider: String = "."
    override var ignore: Boolean = false

    override fun build(): DefaultSchemaClassifier {
        requireNotNull(value)
        requireNotNull(divider)
        requireNotNull(priority)

        return DefaultSchemaClassifier(value = value!!, divider = divider!!, priority = priority!!, componentDivider = componentDivider, ignore = ignore)
    }
}