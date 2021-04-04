package com.uploader.config

data class AppConfig(
    val host: String,
    val port: Int,
    val dbHost: String,
    val dbPort: String,
    val dbUser: String,
    val dbPassword: String,
    val dbName: String
)
