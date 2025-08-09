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

import io.github.vxrpenter.updater.schema.SchemaClassifier
import io.github.vxrpenter.updater.schema.UpdateSchema

interface SchemaBuilder {
    /**
     * Defines the beginning of a version, e.g. `v` or `v.`
     */
    var prefixes: Collection<String>

    /**
     * The symbol that is used to divide the version components, e.g. `.` or `-`
     */
    var divider: String

    /**
     * A collection of possible [SchemaClassifier]
     */
    var classifiers: MutableCollection<SchemaClassifier>

    fun classifier(builder: SchemaClassifierBuilder.() -> Unit)

    fun customClassifier(classifier: SchemaClassifier)

    fun build(): UpdateSchema
}