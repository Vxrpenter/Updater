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

@file:Suppress("unused")

package io.github.vxrpenter.updater.priority

/**
 * Represents the priority of a value/task.
 *
 * Negative and positive priorities are possible as they are all converted to Double
 * for comparisons.
 * You are also able to convert Longs to priority but note that they
 * will also be converted to Double.
 */
@JvmInline
value class Priority(private val rawValue: Double) : Comparable<Priority> {
    companion object {
        /** Returns a [Priority] equal to this [Int] converted to double. */
        inline val Int.priority : Priority get() = toPriority()

        /** Returns a [Priority] equal to this [Long] converted to double. */
        inline val Long.priority : Priority get() = toPriority()

        /** Returns a [Priority] equal to this [Double]. */
        inline val Double.priority : Priority get() = toPriority()

        /** Returns a [Priority] equal to this [Int] converted to double. */
        fun Int.toPriority(): Priority = Priority(this.toDouble())

        /** Returns a [Priority] equal to this [Long] converted to double. */
        fun Long.toPriority() : Priority = Priority(this.toDouble())

        /** Returns a [Priority] equal to this [Double]. */
        fun Double.toPriority(): Priority = Priority(this)
    }

    /** Converts the priority into an integer */
    fun toInt(): Int = this.rawValue.toInt()

    /** Converts the priority into a long */
    fun toLong(): Long = this.rawValue.toLong()

    /** Converts the priority into a double */
    fun toDouble(): Double = this.rawValue

    /**
     * Returns priority whose value is the sum of this ann [other] priority values.
     */
    operator fun plus(other: Priority): Priority = this.rawValue.plus(other.rawValue).toPriority()

    /** Returns priority whose value is the difference between this ann [other] priority values. */
    operator fun minus(other: Priority): Priority = this.rawValue.minus(other.rawValue).toPriority()

    /** Returns priority whose value is the priority value, multiplied by the given [scale] number. */
    operator fun times(scale: Int): Priority = this.rawValue.times(scale).toPriority()

    /** Returns priority whose value is the priority value, multiplied by the given [scale] number */
    operator fun times(scale: Double): Priority = this.rawValue.times(scale).toPriority()

    /** Returns a priority whose value is this priority value divided by the given [scale] number. */
    operator fun div(scale: Int): Priority = this.rawValue.div(scale).toPriority()

    /** Returns a priority whose value is this priority value divided by the given [scale] number. */
    operator fun div(scale: Double): Priority = this.rawValue.div(scale).toPriority()

    /** Returns true if the priority value is less than zero. */
    fun isNegative(): Boolean = this.rawValue < 0

    /** Returns true if the priority value is greater than zero. */
    fun isPositive(): Boolean = this.rawValue > 0

    override fun compareTo(other: Priority): Int = this.rawValue.compareTo(other.rawValue)
}