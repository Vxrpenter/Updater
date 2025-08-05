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

package io.github.vxrpenter.updater.schema

import io.github.vxrpenter.updater.priority.Priority

data class HangarSchemaClassifier(
    override val value: String,
    override val divider: String,
    override val componentDivider: String,
    override val priority: Priority,
    /**
     * The custom channel that the version is located in
     */
    val channel: String?,
    override val ignore: Boolean
) : SchemaClassifier