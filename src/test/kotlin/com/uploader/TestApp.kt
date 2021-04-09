package com.uploader

import com.uploader.container.TestDatabase
import java.io.Closeable
import java.io.File
import mu.KLogging
import org.awaitility.Awaitility.await
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.context.GlobalContext

@KoinApiExtension
class TestApp(environment: String = "dev") : Closeable, KoinComponent {
    val config = TestConfigProvider[environment]

    private val database: TestDatabase = TestDatabase(config.dbPort)
    private val app: App = App(config)

    init {
        app.start()
        await().until { GlobalContext.getOrNull() != null }
    }

    init {
        logger.info { "App started on port: ${config.port} and DB port: ${config.dbPort}" }
    }

    override fun close() {
        val file = File(config.rootBuildsPath)
        file.deleteRecursively()

        app.stop()
        database.close()

        logger.debug { "App was finished gracefully" }
    }

    private companion object : KLogging()
}
