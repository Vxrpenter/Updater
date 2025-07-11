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

package io.github.vxrpenter.builder

import io.github.vxrpenter.annotations.Internal
import io.github.vxrpenter.data.SchemaGroup
import io.github.vxrpenter.data.UpdateSchema
import io.github.vxrpenter.data.Upstream
import io.github.vxrpenter.enum.GroupPriority
import io.github.vxrpenter.enum.ModrinthProjectType
import io.github.vxrpenter.enum.UpstreamType

/**
 * The [Schema] function is an easy way to creating a [UpdateSchema], by providing simple solutions and
 * an easy-to-understand format. If you want to use a more complex function, you can use the [SchemaBuilder].
 *
 * Example Usage:
 * ```kotlin
 * val schema = Schema {
 *     name = "MyCustomSchema"
 *     prefix = "v."
 *     group {
 *         name = "alpha"
 *         divider = "-"
 *         priority = GroupPriority.LOW
 *     }
 *     group {
 *         name = "beta"
 *         divider = "-"
 *         priority = GroupPriority.HIGH
 *     }
 * }
 * ```
 *
 * @param name The name of the [UpdateSchema]
 * @param groups A list of [SchemaGroup]
 * @param builder The [SchemaBuilder] (ignore)
 *
 * @author Vxrpenter
 * @since 0.1.0
 */
inline fun Schema(
    name: String? = null,
    prefix: String? = null,
    divider: String = ".",
    groups: Collection<SchemaGroup> = emptyList(),
    @OptIn(Internal::class)
    builder: SchemaBuilder.() -> Unit = {},
) : UpdateSchema {
    val internalBuilder = SchemaBuilder()

    name?.let { internalBuilder.name = name }
    prefix?.let { internalBuilder.prefix = prefix }
    groups.isEmpty().let { internalBuilder.groups = groups.toMutableList() }

    internalBuilder.builder()
    val schema = internalBuilder.build()
    return schema
}

/**
 * The [Upstream] function is an easy way to create a [io.github.vxrpenter.data.Upstream] configuration for the
 * [ConfigurationBuilder]/[io.github.vxrpenter.Updater.light] or [io.github.vxrpenter.Updater.default] functions.
 *
 * Example Usage:
 * ```kotlin
 * val upstream = Upstream {
 *     type = UpstreamType.GITHUB
 *     repository = "https://github.com/Vxrpenter/Updater"
 *     // This is only important if release tags are uploaded elsewhere (must still be in the selected
 *     // types api format)
 *     tagUrl = "https://api.github.com/repos/Vxrpenter/Updater/git/refs/tags"
 * }
 * ```
 *
 * @param type The [UpstreamType] of the upstream
 * @param repository The link to the upstream repository (only needed for GitHub and GitLab)
 * @param tagUrl The link to the upstream tags (only needed for GitHub and GitLab)
 * @param builder The [UpstreamBuilder] (ignore)
 *
 * @author Vxrpenter
 * @since 0.1.0
 */
inline fun Upstream(
    type: UpstreamType? = null,
    projectId: String? = null,
    modrinthProjectType: ModrinthProjectType? = null,
    builder: UpstreamBuilder.() -> Unit
): Upstream {
    val internalBuilder = UpstreamBuilder()
    internalBuilder.builder()

    type?.let { internalBuilder.type = type }
    projectId?.let { internalBuilder.projectId = projectId }
    modrinthProjectType?.let { internalBuilder.modrinthProjectType = modrinthProjectType }

    val upstream = internalBuilder.build()
    return upstream
}

class SchemaBuilder {
    var name: String? = null
    var prefix: String? = null
    var divider: String? = null
    var groups: MutableCollection<SchemaGroup> = mutableListOf()

    inline fun group(
        name: String? = null,
        divider: String? = null,
        priority: GroupPriority? = null,
        channel: String? = null,
        build: InlineSchemaGroup.() -> Unit
    ) {
        val group = InlineSchemaGroup(name = name, priority = priority, divider = divider, channel = channel).apply(build)
        require(!group.name.isNullOrBlank()) { "SchemaGroup.Name cannot be null" }
        require(group.priority != null) { "'SchemaGroup.priority' cannot be null" }

        groups.add(SchemaGroup(name = group.name!!, priority =  group.priority!!, divider = group.divider, channel = group.channel))
    }

    data class InlineSchemaGroup(
        var name: String? = null,
        var divider: String? = null,
        var priority: GroupPriority? = null,
        var channel: String? = null
    )

    fun build(): UpdateSchema {
        require(!this.name.isNullOrBlank()) { "'name' cannot be null" }
        require(!this.prefix.isNullOrBlank()) { "'prefix' cannot be null" }
        require(!this.groups.isEmpty()) { "'groups' cannot be empty" }
        require(!this.divider.isNullOrBlank()) { "'divider' cannot be empty" }

        return UpdateSchema(name = name!!, prefix = prefix!!, divider = divider!!, groups = groups.toList())
    }
}

class UpstreamBuilder {
    var type: UpstreamType? = null
    var projectId: String? = null
    var modrinthProjectType: ModrinthProjectType? = null

    fun build(): Upstream {
        require(this.type != null) { "'type' cannot be null" }
        require(this.projectId != null) { "'projectId' cannot be null" }
        if (type!! == UpstreamType.MODRINTH) require(this.modrinthProjectType != null) { "'modrinthProjectType' cannot be null" }

        return Upstream(type = type!!, projectId = projectId)
    }
}