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

package io.github.vxrpenter.updater.builder

import io.github.vxrpenter.updater.data.SchemaClassifier
import io.github.vxrpenter.updater.data.UpdateSchema
import io.github.vxrpenter.updater.enum.ClassifierPriority

/**
 * The [Schema] function is an easy way to creating a [UpdateSchema], by providing simple solutions and
 * an easy-to-understand format. If you want to use a more complex function, you can use the [SchemaBuilder].
 *
 * Example Usage:
 * ```kotlin
 * val schema = Schema {
 *     name = "MyCustomSchema"
 *     prefix = "v"
 *     classifier {
 *         name = "alpha"
 *         divider = "-"
 *         priority = GroupPriority.LOW
 *     }
 *     classifier {
 *         name = "beta"
 *         divider = "-"
 *         priority = GroupPriority.HIGH
 *     }
 * }
 * ```
 *
 * @param [SchemaBuilder.name] The name of the [UpdateSchema]
 * @param [SchemaBuilder.prefix] The removable prefix
 * @param [SchemaBuilder.classifiers] A list of [SchemaClassifier]
 * @param builder The [SchemaBuilder] (ignore)
 *
 * @author Vxrpenter
 * @since 0.1.0
 */
inline fun Schema(
    builder: SchemaBuilder.() -> Unit = {},
) : UpdateSchema {
    val internalBuilder = SchemaBuilder()
    internalBuilder.builder()
    val schema = internalBuilder.build()
    return schema
}

class SchemaBuilder {
    var name: String? = null
    var prefix: String? = null
    var divider: String? = null
    var classifiers: MutableCollection<SchemaClassifier> = mutableListOf()

    inline fun classifier(
        name: String? = null,
        divider: String? = null,
        priority: ClassifierPriority? = null,
        channel: String? = null,
        build: InlineSchemaClassifier.() -> Unit
    ) {
        val classifier = InlineSchemaClassifier(name = name, priority = priority, divider = divider, channel = channel).apply(build)
        requireNotNull(!classifier.name.isNullOrBlank())
        requireNotNull(classifier.priority != null)

        classifiers.add(
            SchemaClassifier(
                name = classifier.name!!,
                priority = classifier.priority!!,
                divider = classifier.divider,
                channel = classifier.channel
            )
        )
    }

    data class InlineSchemaClassifier(
        var name: String? = null,
        var divider: String? = null,
        var priority: ClassifierPriority? = null,
        var channel: String? = null
    )

    fun build(): UpdateSchema {
        requireNotNull(this.name)
        requireNotNull(this.prefix)
        require(this.prefix!!.isNotEmpty()) { "'prefix' cannot be empty" }
        require(!this.classifiers.isEmpty()) { "'classifiers' cannot be empty" }
        require(!this.divider.isNullOrBlank()) { "'divider' cannot be empty" }

        return UpdateSchema(name = name!!, prefix = prefix!!, divider = divider!!, classifiers = classifiers.toList())
    }
}