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

package io.github.vxrpenter.enum

enum class ModrinthProjectType(val type: String) {
    MOD("mod"),
    RESOURCEPACK("resourcepack"),
    DATAPACK("datapack"),
    SHADER("shader"),
    MODPACK("modpack"),
    PLUGIN("plugin");

    companion object {
        /**
         * Finds the specified enum name from its ModrinthProjectType.
         *
         * @param value The ModrinthProjectType e.g. (modpack, plugin, etc.)
         * @see io.github.vxrpenter.enum.GroupPriority
         * @return the ModrinthProjectType (nullable)
         */
        fun findEnum(value: String): ModrinthProjectType? = ModrinthProjectType.entries.find { it.type == value }

        /**
         * Finds the specified ModrinthProjectType from its enum
         *
         * @param enum The enum e.g. (MODPACK, PLUGIN etc.)
         * @see io.github.vxrpenter.enum.ModrinthProjectType
         * @return the ModrinthProjectType (nullable)
         */
        fun findValue(enum: ModrinthProjectType): String? = ModrinthProjectType.entries.find { it.name == enum.name }?.type
    }
}