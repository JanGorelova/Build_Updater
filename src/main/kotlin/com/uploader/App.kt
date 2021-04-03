package com.uploader

import com.typesafe.config.ConfigFactory
import com.uploader.config.AppConfig
import com.uploader.module.AppModule.module
import com.uploader.schedule.CheckNewBuildsTask
import com.uploader.schedule.DownloadBuildsTask
import com.uploader.schedule.Job
import com.uploader.schedule.PersistProductInfoTask
import io.ktor.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.*
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.koin.core.component.KoinApiExtension
import java.time.Duration

@ObsoleteCoroutinesApi
class App(
    private val environment: String
) {
    private val config: AppConfig = extractConfig(environment, HoconApplicationConfig(ConfigFactory.load()))

    @KoinApiExtension
    @KtorExperimentalAPI
    fun start() : NettyApplicationEngine =
        embeddedServer(Netty, port = config.port, host = config.host) {
            this.module(config)
        }.start(false)
            .also {
                Job(CheckNewBuildsTask(), "Build info update")
                Job(DownloadBuildsTask(), "Build download", delay = Duration.ofMinutes(1), period = Duration.ofSeconds(30))
                Job(PersistProductInfoTask(), "Build info persist", delay = Duration.ofMinutes(2), period = Duration.ofSeconds(30))
            }

    @KtorExperimentalAPI
    private fun extractConfig(environment: String, hoconConfig: HoconApplicationConfig): AppConfig {
        val hoconEnvironment = hoconConfig.config("ktor.deployment.$environment")
        return AppConfig(
            hoconEnvironment.property("host").getString(),
            Integer.parseInt(hoconEnvironment.property("port").getString()),
            hoconEnvironment.property("databaseHost").getString(),
            hoconEnvironment.property("databasePort").getString(),
            hoconEnvironment.property("databaseUser").getString(),
            hoconEnvironment.property("databasePassword").getString(),
            hoconEnvironment.property("databaseName").getString(),
        )
    }
}