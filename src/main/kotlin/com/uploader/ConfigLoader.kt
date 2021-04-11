package com.uploader

import com.typesafe.config.ConfigFactory
import com.uploader.config.AppConfig
import com.uploader.config.AppConfig.JobConfig
import com.uploader.module.JobType
import io.ktor.config.ApplicationConfig
import io.ktor.config.HoconApplicationConfig
import java.time.Duration

class ConfigLoader {
    private val factory = HoconApplicationConfig(ConfigFactory.load())

    fun extractConfig(): AppConfig {
        val hoconEnvironment = factory.config("ktor.deployment.dev")
        val buildsPath = hoconEnvironment.property("buildsPath").getString()
        return AppConfig(
            hoconEnvironment.property("host").getString(),
            Integer.parseInt(hoconEnvironment.property("port").getString()),
            hoconEnvironment.property("databaseHost").getString(),
            Integer.parseInt(hoconEnvironment.property("databasePort").getString()),
            hoconEnvironment.property("databaseUser").getString(),
            hoconEnvironment.property("databasePassword").getString(),
            hoconEnvironment.property("databaseName").getString(),
            extractJobConfig(hoconEnvironment),
            buildsPath
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
