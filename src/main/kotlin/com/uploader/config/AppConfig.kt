package com.uploader.config

import java.time.Duration

data class AppConfig(
    val host: String,
    val port: Int,
    val dbHost: String,
    val dbPort: String,
    val dbUser: String,
    val dbPassword: String,
    val dbName: String,
    val jobs: Map<String, JobConfig> = mapOf()
) {
    data class JobConfig(
        val delay: Duration = Duration.ofSeconds(10),
        val period: Duration = Duration.ofSeconds(20)
    )
}
