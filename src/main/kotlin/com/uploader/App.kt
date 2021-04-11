package com.uploader

import com.uploader.config.AppConfig
import com.uploader.module.AppModule.module
import com.uploader.schedule.Job
import io.ktor.application.uninstallAllFeatures
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import mu.KLogging
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.context.stopKoin

@KoinApiExtension
class App(val config: AppConfig) : KoinComponent {
    private val engine: NettyApplicationEngine =
        embeddedServer(Netty, port = config.port, host = config.host) {
            logger.info { "Config: $config" }
            this.module(config)
        }

    @KoinApiExtension
    fun start() {
        engine.start(false)
    }

    fun stop() {
        val jobs = getKoin().getAll<Job>()
        jobs.forEach { it.cancel() }

        engine.application.uninstallAllFeatures()
        engine.stop(10, 10)
        stopKoin()
    }

    private companion object : KLogging()
}
