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

package io.github.vxrpenter.updater.enum

enum class UpstreamPriority(val priority: Int) {
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
         * @param value The UpstreamPriority e.g. (4, 5, etc.)
         * @see io.github.vxrpenter.updater.enum.UpstreamPriority
         * @return the UpstreamPriority (nullable)
         */
        fun findEnum(value: Int): UpstreamPriority? = entries.find { it.priority == value }

        /**
         * Finds the specified UpstreamPriority from its enum
         *
         * @param enum The enum e.g. (HIGH, HIGHEST etc.)
         * @see io.github.vxrpenter.updater.enum.UpstreamPriority
         * @return the UpstreamPriority (nullable)
         */
        fun findValue(enum: UpstreamPriority): Int? = entries.find { it.name == enum.name }?.priority
    }
}