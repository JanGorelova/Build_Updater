package com.uploader

import com.typesafe.config.ConfigFactory
import com.uploader.config.AppConfig
import com.uploader.module.AppModule.module
import com.uploader.schedule.DownloadBuildsTask
import com.uploader.schedule.Job
import com.uploader.schedule.PersistProductInfosTask
import com.uploader.schedule.RefreshProductsInformationTask
import io.ktor.config.HoconApplicationConfig
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import java.time.Duration
import org.koin.core.component.KoinApiExtension

class App(environment: String) {
    private val config: AppConfig = extractConfig(environment, HoconApplicationConfig(ConfigFactory.load()))

    @KoinApiExtension
    fun start(): NettyApplicationEngine =
        embeddedServer(Netty, port = config.port, host = config.host) { this.module(config) }
            .start(false)
            .also {
                Job(RefreshProductsInformationTask(), "Build info update")
                Job(
                    task = DownloadBuildsTask(),
                    name = "Build download",
                    delay = Duration.ofMinutes(0),
                    period = Duration.ofSeconds(5)
                )
                Job(
                    task = PersistProductInfosTask(),
                    name = "Build info persist",
                    delay = Duration.ofMinutes(0),
                    period = Duration.ofSeconds(5)
                )
            }

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
