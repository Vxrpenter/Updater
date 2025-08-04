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

package io.github.vxrpenter.updater.priority

@JvmInline
value class Priority(private val rawValue: Double) : Comparable<Priority> {
    companion object {
        inline val Int.priority : Priority get() = toPriority()
        inline val Long.priority : Priority get() = toPriority()
        inline val Double.priority : Priority get() = toPriority()

        fun Int.toPriority(): Priority = Priority(this.toDouble())
        fun Long.toPriority() : Priority = Priority(this.toDouble())
        fun Double.toPriority(): Priority = Priority(this)
    }

    override fun compareTo(other: Priority): Int = this.rawValue.compareTo(other.rawValue)
}