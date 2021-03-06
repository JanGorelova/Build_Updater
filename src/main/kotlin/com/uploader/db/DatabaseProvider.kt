package com.uploader.db

import com.uploader.dao.entity.Build
import com.uploader.dao.entity.BuildInfo
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
    private val hicari by inject<HikariDataSource>()
    private val dispatcher: CoroutineContext

    init {
        dispatcher = Dispatchers.Default
        init()
    }

    private fun init() {
        Database.connect(hicari)
        transaction {
            create(BuildInfo, Build)
        }
    }

    suspend fun <T> dbQuery(block: () -> T): T = withContext(dispatcher) {
        transaction { block() }
    }
}
