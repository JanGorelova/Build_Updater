package com.uploader

import com.uploader.config.AppConfig
import com.uploader.module.HicariProvider.hikari
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

class TestDatabaseProvider(config: AppConfig) {
    private val hicari = hikari(config)
    private val dispatcher: CoroutineContext

    init {
        dispatcher = Dispatchers.Default
        init()
    }

    private fun init() {
        Database.connect(hicari)
    }

    suspend fun <T> dbQuery(block: () -> T): T = withContext(dispatcher) {
        transaction { block() }
    }

    fun close() {
        hicari.close()
    }
}
