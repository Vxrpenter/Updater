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

@file:Suppress("unused")

package io.github.vxrpenter.enum

enum class GroupPriority(val priority: Int) {
    NONE(0),
    MINIMAL(1),
    LOW(2),
    MIDDLE(3),
    HIGH(4),
    HIGHEST(5);

    companion object {
        /**
         * Finds the specified enum name from its GroupPriority.
         *
         * @param value The GroupPriority e.g. (4, 5, etc.)
         * @see io.github.vxrpenter.enum.GroupPriority
         * @return the GroupPriority (nullable)
         */
        fun findEnum(value: Int): GroupPriority? = GroupPriority.entries.find { it.priority == value }

        /**
         * Finds the specified GroupPriority from its enum
         *
         * @param enum The enum e.g. (HIGH, HIGHEST etc.)
         * @see io.github.vxrpenter.enum.GroupPriority
         * @return the GroupPriority (nullable)
         */
        fun findValue(enum: GroupPriority): Int? = GroupPriority.entries.find { it.name == enum.name }?.priority
    }
}