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

@file:Suppress("unused", "FunctionName")

package io.github.vxrpenter.updater

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.vxrpenter.updater.annotations.ExperimentalScheduler
import io.github.vxrpenter.updater.configuration.builder.ConfigurationBuilder
import io.github.vxrpenter.updater.configuration.UpdaterConfiguration
import io.github.vxrpenter.updater.internal.AutoUpdater
import io.github.vxrpenter.updater.internal.UpdateChecker
import io.github.vxrpenter.updater.schema.UpdateSchema
import io.github.vxrpenter.updater.upstream.Upstream
import io.github.vxrpenter.updater.priority.Priority
import io.github.vxrpenter.updater.update.Update
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import kotlin.time.toJavaDuration

/**
 * Compares versions fetched from
 * specific upstream ([Upstream]) with other versions.
 *
 * @param configuration of the updater
 */
open class Updater(private var configuration: UpdaterConfiguration) {
    private val logger = KotlinLogging.logger {}
    private val updatesScope = CoroutineScope(CoroutineExceptionHandler { _, exception -> logger.error(exception) { "An error occurred in the update coroutine" } }
            + Executors.newCachedThreadPool().asCoroutineDispatcher())

    /**
     * An [HttpClient], that is configured using the [configuration].
     * This client will be passed onto all upstream fetching logic to execute calls.
     */
    private var client: HttpClient? = null

    /**
     * The default updater object using the default configuration.
     * Configuration can be changed using the builders in the functions.
     */
    companion object Default : Updater(configuration = UpdaterConfiguration())

    /**
     * Returns an update when a version update was found from the supplied upstream.
     * When no version could be returned, null will be returned.
     *
     * @param currentVersion complete version of the application
     * @param schema defines the version deserialization
     * @param upstream the upstream to fetch the version from
     * @param builder the [ConfigurationBuilder]
     * @return an [Update]
     */
    suspend fun getUpdate(currentVersion: String, schema: UpdateSchema, upstream: Upstream, builder: (ConfigurationBuilder.() -> Unit)? = null): Update? {
        if (builder != null) runBuilder(builder)
        if (client == null) client = createClient()

        return UpdateChecker(configuration, client!!).getUpdate(currentVersion = upstream.toVersion(currentVersion, schema), schema = schema, upstream = upstream)
    }

    /**
     * Returns a list of updates from a collection of upstream's.
     * When no version could be returned, the collection will be empty.
     *
     * @param currentVersion complete version of the application
     * @param schema defines the version deserialization
     * @param upstreams a collection of upstreams that versions will be fetched from
     * @param builder the [ConfigurationBuilder]
     * @return a [Collection] of [Update]
     */
    suspend fun getMultipleUpdates(currentVersion: String, schema: UpdateSchema, upstreams: Collection<Upstream>, builder: (ConfigurationBuilder.() -> Unit)? = null): Collection<Update> {
        if (builder != null) runBuilder(builder)
        if (client == null) client = createClient()

        return UpdateChecker(configuration, client!!).getMultipleUpdates(currentVersion = currentVersion, schema = schema, upstreams = upstreams)
    }

    /**
     * Checks for new updates by fetching the version from the specified upstream,
     * then it compares the current version to the fetched one and (when enabled)
     * returns a notification.
     *
     * @param currentVersion complete version of the application
     * @param schema defines the version deserialization
     * @param upstream the upstream to fetch the version from
     * @param builder the [ConfigurationBuilder]
     */
    @OptIn(ExperimentalScheduler::class)
    fun checkUpdates(currentVersion: String, schema: UpdateSchema, upstream: Upstream, builder: (ConfigurationBuilder.() -> Unit)? = null) {
        if (builder != null) runBuilder(builder)
        if (client == null) client = createClient()

        start { UpdateChecker(configuration, client!!).checkForUpdate(currentVersion = upstream.toVersion(currentVersion, schema), schema = schema, upstream = upstream) }
    }

    /**
     * Checks for new updates by fetching versions from multiple upstreams.
     * It then compares the versions by first checking for the biggest returned version.
     *
     * If the returned versions from at least 2 upstreams are equal, the prioritized
     * upstream will be selected by comparing them using their [Priority].
     *
     * @param currentVersion complete version of the application
     * @param schema defines the version deserialization
     * @param upstreams a collection of upstreams that versions will be fetched from
     * @param builder the [ConfigurationBuilder]
     */
    @OptIn(ExperimentalScheduler::class)
    fun checkMultipleUpdates(currentVersion: String, schema: UpdateSchema, upstreams: Collection<Upstream>, builder: (ConfigurationBuilder.() -> Unit)? = null) {
        if (builder != null) runBuilder(builder)
        if (client == null) client = createClient()

        start { UpdateChecker(configuration, client!!).checkMultipleUpdates(currentVersion = currentVersion, schema = schema, upstreams = upstreams) }
    }

    @Deprecated("Do not use, currently not fully implemented")
    fun autoUpdate(currentVersion: String, schema: UpdateSchema, upstream: Upstream, builder: (ConfigurationBuilder.() -> Unit)? = null) {
        if (builder != null) runBuilder(builder)
        if (client == null) client = createClient()

        start { AutoUpdater(configuration, client!!).checkForUpdate(currentVersion = upstream.toVersion(currentVersion, schema), schema = schema, upstream = upstream) }
    }

    private fun createClient(): HttpClient = HttpClient(engineFactory = OkHttp) {
        install(plugin = ContentNegotiation) {
            json(json = configuration.json)
        }

        engine { config {
            readTimeout(duration = configuration.readTimeout.toJavaDuration())
            writeTimeout(duration = configuration.writeTimeout.toJavaDuration())
        }}
    }

    @OptIn(ExperimentalScheduler::class)
    private fun start(task: suspend () -> Unit) {
        if (configuration.periodic != null) {
            updatesScope.scheduleWithDelay(period = configuration.periodic!!) { task() }
            return
        }

        updatesScope.launch { task() }
    }

    private fun runBuilder(builder: ConfigurationBuilder.() -> Unit) {
        val internalBuilder = ConfigurationBuilder()
        internalBuilder.builder()
        configuration = internalBuilder.build()
        client = createClient()
    }
}