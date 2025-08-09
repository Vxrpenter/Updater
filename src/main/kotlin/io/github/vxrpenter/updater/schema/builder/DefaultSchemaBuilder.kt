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

package io.github.vxrpenter.updater.schema.builder

import io.github.vxrpenter.updater.priority.Priority
import io.github.vxrpenter.updater.schema.DefaultSchemaClassifier
import io.github.vxrpenter.updater.schema.DefaultUpdateSchema
import io.github.vxrpenter.updater.schema.SchemaClassifier

/**
 * The [Schema] function is an easy way to creating a [io.github.vxrpenter.updater.schema.DefaultUpdateSchema], by providing simple solutions and
 * an easy-to-understand format.
 * If you want to use a more complex function, you can use the [DefaultSchemaBuilder].
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
 * @param [DefaultSchemaBuilder.prefixes] Defines the beginning of a version, e.g. `v` or `v.`
 * @param [DefaultSchemaBuilder.divider] The symbol that is used to divide the version components, e.g. `.` or `-`
 * @param [DefaultSchemaBuilder.classifier] Version classifier
 *
 * @return the [io.github.vxrpenter.updater.schema.DefaultUpdateSchema]
 * @see io.github.vxrpenter.updater.schema.UpdateSchema
 */
inline fun Schema(builder: DefaultSchemaBuilder.() -> Unit = {}) : DefaultUpdateSchema {
    val internalBuilder = DefaultSchemaBuilder()
    internalBuilder.builder()
    val schema = internalBuilder.build()
    return schema
}

class DefaultSchemaBuilder : SchemaBuilder {
    override var prefixes: Collection<String> = emptyList()
    /**
     * Add one prefix to[prefixes]
     */
    fun addPrefix(prefix: String) = apply {
        val prefixList = prefixes.toMutableList()
        prefixList.add(prefix)
        prefixes = prefixList
    }

    override var divider: String = "."
    /**
     * Set the [divider]
     */
    fun setDivider(divider: String) = apply { this.divider = divider }

    override var classifiers: MutableCollection<SchemaClassifier> = mutableListOf()


    /**
     * A [io.github.vxrpenter.updater.schema.DefaultSchemaClassifier]
     *
     * @see SchemaClassifier
     */
    override fun classifier(
        builder: SchemaClassifierBuilder.() -> Unit
    ) {
        val classifier = DefaultSchemaClassifierBuilder().apply(builder)
        requireNotNull(classifier.value)
        requireNotNull(classifier.divider)
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
    /**
     * Add one classifier to [classifiers]
     */
    fun addClassifier(value: String, divider: String, priority: Priority, componentDivider: String = ".", ignore: Boolean = false) = apply {
        classifiers.add(
            DefaultSchemaClassifier(
                value = value,
                divider = divider,
                componentDivider = componentDivider,
                priority = priority,
                ignore = ignore
            )
        )
    }

    override fun customClassifier(classifier: SchemaClassifier) {
        classifiers.add(classifier)
    }

    override fun build(): DefaultUpdateSchema {
        require(!this.prefixes.isEmpty()) { "'prefixes' cannot be empty" }
        require(!this.classifiers.isEmpty()) { "'classifiers' cannot be empty" }

        return DefaultUpdateSchema(prefixes = prefixes, divider = divider, classifiers = classifiers.toList())
    }
}