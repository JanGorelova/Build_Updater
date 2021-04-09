package com.uploader.config

import com.uploader.module.JobType
import java.time.Duration

data class AppConfig(
    val host: String,
    val port: Int,
    val dbHost: String,
    val dbPort: Int,
    val dbUser: String,
    val dbPassword: String,
    val dbName: String,
    val jobs: Map<JobType, JobConfig>,
    val rootBuildsPath: String
) {
    data class JobConfig(
        val delay: Duration = Duration.ofSeconds(10),
        val period: Duration = Duration.ofSeconds(20),
        val enabled: Boolean = true
    )
}
