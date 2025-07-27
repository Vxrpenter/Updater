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

package io.github.vxrpenter.updater.schema

/**
 * The [Schema] function is an easy way to creating a [DefaultUpdateSchema], by providing simple solutions and
 * an easy-to-understand format. If you want to use a more complex function, you can use the [SchemaBuilder].
 *
 * Example Usage:
 * ```kotlin
 * val schema = Schema {
 *     prefix = "v"
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
 * @param [SchemaBuilder.prefix] The removable prefix
 * @param [SchemaBuilder.classifiers] A list of [DefaultSchemaClassifier]
 * @param builder The [SchemaBuilder] (ignore)
 *
 * @author Vxrpenter
 * @since 0.1.0
 */
inline fun Schema(
    builder: SchemaBuilder.() -> Unit = {},
) : DefaultUpdateSchema {
    val internalBuilder = SchemaBuilder()
    internalBuilder.builder()
    val schema = internalBuilder.build()
    return schema
}

class SchemaBuilder {
    var prefix: String? = null
    var divider: String = "."
    var classifiers: MutableCollection<DefaultSchemaClassifier> = mutableListOf()

    inline fun classifier(
        value: String? = null,
        divider: String? = null,
        priority: ClassifierPriority? = null,
        componentDivider: String = ".",
        channel: String? = null,
        build: InlineSchemaClassifier.() -> Unit
    ) {
        val classifier = InlineSchemaClassifier(priority = priority, divider = divider, channel = channel).apply(build)
        requireNotNull(classifier.value)
        requireNotNull(classifier.priority)

        classifiers.add(
            DefaultSchemaClassifier(
                value = classifier.value!!,
                priority = classifier.priority!!,
                divider = classifier.divider!!,
                componentDivider = classifier.componentDivider,
                channel = classifier.channel
            )
        )
    }

    data class InlineSchemaClassifier(
        var value: String? = null,
        var divider: String? = null,
        var priority: ClassifierPriority? = null,
        var componentDivider: String = ".",
        var channel: String? = null
    )

    fun build(): DefaultUpdateSchema {
        requireNotNull(this.prefix)
        require(this.prefix!!.isNotEmpty()) { "'prefix' cannot be empty" }
        require(!this.classifiers.isEmpty()) { "'classifiers' cannot be empty" }

        return DefaultUpdateSchema(prefix = prefix!!, divider = divider, classifiers = classifiers.toList())
    }
}