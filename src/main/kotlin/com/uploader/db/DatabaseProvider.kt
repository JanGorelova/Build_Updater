package com.uploader.db

import com.uploader.config.AppConfig
import com.uploader.dao.entity.Build
import com.uploader.dao.entity.BuildInfo
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class DatabaseProvider : KoinComponent {
    private val appConfig by inject<AppConfig>()
    private val dispatcher: CoroutineContext

    init {
        dispatcher = Dispatchers.Default
        init()
    }

    private fun init() {
        Database.connect(hikari())
        transaction {
            create(BuildInfo, Build)
        }
    }

    private fun hikari(): HikariDataSource {
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

    suspend fun <T> dbQuery(block: () -> T): T = withContext(dispatcher) {
        transaction { block() }
    }
}
