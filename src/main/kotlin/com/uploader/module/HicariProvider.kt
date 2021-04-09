package com.uploader.module

import com.uploader.config.AppConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

object HicariProvider {
    fun hikari(appConfig: AppConfig): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName = "org.postgresql.Driver"
        config.jdbcUrl = "jdbc:postgresql://${appConfig.dbHost}:${appConfig.dbPort}/${appConfig.dbName}"
        config.username = appConfig.dbUser
        config.password = appConfig.dbPassword
        config.maximumPoolSize = 3
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        config.validate()
        return HikariDataSource(config)
    }
}
