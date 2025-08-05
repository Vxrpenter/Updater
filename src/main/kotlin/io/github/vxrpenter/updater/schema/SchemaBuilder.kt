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

package io.github.vxrpenter.updater.schema

import io.github.vxrpenter.updater.priority.Priority
import io.github.vxrpenter.updater.version.Version

/**
 * The [Schema] function is an easy way to creating a [DefaultUpdateSchema], by providing simple solutions and
 * an easy-to-understand format. If you want to use a more complex function, you can use the [SchemaBuilder].
 *
 * Example Usage:
 * ```kotlin
 * val schema = Schema {
 *     prefix = "v"
 *     divider = "."
 *     classifier {
 *         value = "alpha"
 *         divider = "-"
 *         priority = ClassifierPriority.LOW
 *     }
 *     classifier {
 *         value = "beta"
 *         divider = "-"
 *         priority = ClassifierPriority.HIGH
 *     }
 *     classifier {
 *         value = "rc"
 *         divider = "-"
 *         priority = ClassifierPriority.HIGHEST
 *     }
 * }
 * ```
 *
 * @param [SchemaBuilder.prefixes] Defines the beginning of a version, e.g. `v` or `v.`
 * @param [SchemaBuilder.divider] The symbol that is used to divide the version components, e.g. `.` or `-`
 * @param [SchemaBuilder.classifier] Version classifier
 *
 * @return the [DefaultUpdateSchema]
 * @see UpdateSchema
 */
inline fun Schema(
    builder: SchemaBuilder.() -> Unit = {}
) : DefaultUpdateSchema {
    val internalBuilder = SchemaBuilder()
    internalBuilder.builder()
    val schema = internalBuilder.build()
    return schema
}

class SchemaBuilder {
    /**
     * Defines the beginning of a version, e.g. `v` or `v.`
     */
    var prefixes: Collection<String>? = null
    /**
     * The symbol that is used to divide the version components, e.g. `.` or `-`
     */
    var divider: String = "."
    private var classifiers: MutableCollection<SchemaClassifier> = mutableListOf()


    /**
     * A [DefaultSchemaClassifier]
     *
     * @see SchemaClassifier
     */
    fun classifier(
        builder: InlineSchemaClassifier.() -> Unit
    ) {
        val classifier = InlineSchemaClassifier().apply(builder)
        requireNotNull(classifier.value)
        requireNotNull(classifier.priority)

        classifiers.add(
            DefaultSchemaClassifier(
                value = classifier.value!!,
                divider = classifier.divider!!,
                componentDivider = classifier.componentDivider,
                priority = classifier.priority!!,
                ignore = classifier.ignore
            )
        )
    }

    fun customClassifier(classifier: SchemaClassifier) {
        classifiers.add(classifier)
    }

    data class InlineSchemaClassifier(
        /**
         * Complete classifier string
         */
        var value: String? = null,
        /**
         * Priority of the classifier
         */
        var divider: String? = null,
        /**
         * The symbol that is used to divide the classifier and the [Version],
         * e.g. `.` or `-`
         */
        var priority: Priority? = null,
        /**
         * The symbol that is used to divide the version components, e.g. `.` or `-`
         */
        var componentDivider: String = ".",
        /**
         * Defines if the classifier should be ignored
         */
        var ignore: Boolean = false
    )

    fun build(): DefaultUpdateSchema {
        require(!this.classifiers.isEmpty()) { "'classifiers' cannot be empty" }

        return DefaultUpdateSchema(prefixes = prefixes, divider = divider, classifiers = classifiers.toList())
    }
}