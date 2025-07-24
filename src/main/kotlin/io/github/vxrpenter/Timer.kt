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

package io.github.vxrpenter

import io.github.vxrpenter.annotations.ExperimentalScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration

@ExperimentalScheduler
internal open class Timer{

    companion object Default: Timer()

    fun schedule(period: Duration, coroutineScope: CoroutineScope, task: suspend () -> Unit) = runBlocking {
        var taskExecuted = false

        val currentTask: suspend () -> Unit = {
            task()

            taskExecuted = true
        }

        startTimer(period, coroutineScope, currentTask)
        assert(taskExecuted)
    }

    private fun startTimer(period: Duration, coroutineScope: CoroutineScope, task: suspend () -> Unit) {
        coroutineScope.launch {
            launch {
                while (isActive) {
                    task()
                    delay(period)
                }
            }
        }
    }
}