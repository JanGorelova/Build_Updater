package com.uploader

import com.typesafe.config.ConfigFactory
import com.uploader.config.AppConfig
import com.uploader.config.AppConfig.JobConfig
import com.uploader.module.AppModule.module
import com.uploader.module.JobType
import io.ktor.config.ApplicationConfig
import io.ktor.config.HoconApplicationConfig
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import java.nio.file.Paths
import java.time.Duration
import org.koin.core.component.KoinApiExtension

class App(environment: String) {
    private val config: AppConfig = extractConfig(environment, HoconApplicationConfig(ConfigFactory.load()))

    @KoinApiExtension
    fun start(): NettyApplicationEngine =
        embeddedServer(Netty, port = config.port, host = config.host) { this.module(config) }
            .start(false)

    private fun extractConfig(environment: String, hoconConfig: HoconApplicationConfig): AppConfig {
        val hoconEnvironment = hoconConfig.config("ktor.deployment.$environment")
        val relativeBuildPath = hoconEnvironment.property("relativeBuildsPath").getString()
        return AppConfig(
            hoconEnvironment.property("host").getString(),
            Integer.parseInt(hoconEnvironment.property("port").getString()),
            hoconEnvironment.property("databaseHost").getString(),
            hoconEnvironment.property("databasePort").getString(),
            hoconEnvironment.property("databaseUser").getString(),
            hoconEnvironment.property("databasePassword").getString(),
            hoconEnvironment.property("databaseName").getString(),
            extractJobConfig(hoconEnvironment),
            "${Paths.get("").toRealPath()}$relativeBuildPath"
        )
    }

    private fun extractJobConfig(applicationConfig: ApplicationConfig): Map<JobType, JobConfig> {
        val configs = applicationConfig.configList("jobs")

        return configs
            .map { jobConfig ->
                JobType.valueOf(jobConfig.property("name").getString()) to JobConfig(
                    delay = Duration.parse(jobConfig.property("delay").getString()),
                    period = Duration.parse(jobConfig.property("period").getString()),
                    enabled = jobConfig.property("enabled").getString().toBoolean()
                )
            }.toMap()
    }
}
