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

/**
 * The UpdateSchema interface is a set of instructions
 * on how to deserialize a [io.github.vxrpenter.updater.version.Version] into it's individual components and classifiers.
 */
interface UpdateSchema {
    /**
     * Defines the beginning of a version, e.g. `v` or `v.`
     */
    val prefixes: Collection<String>
    /**
     * The symbol that is used to divide the version components, e.g. `.` or `-`
     */
    val divider: String
    /**
     * A collection of possible [SchemaClassifier]
     */
    val classifiers: Collection<SchemaClassifier>

    /**
     * Removes the prefix from a selected version value.
     *
     * The prefix that is the longest and is contained in the version value will
     * always be chosen first.
     *
     * When no prefix is found, the raw version value will be returned instead
     *
     * @param value the version value to remove the prefix from
     * @return the version value, with removed prefixes
     */
    fun removePrefix(value: String): String {
        val maxLength = prefixes.maxBy { it.toCharArray().size }.toCharArray().size

        for (prefix in prefixes) {
            if (!value.contains(prefix) || prefix.toCharArray().size > maxLength) continue

            return value.replace(prefix, "")
        }

        return value
    }
}