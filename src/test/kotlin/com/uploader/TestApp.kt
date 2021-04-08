package com.uploader

import com.uploader.config.AppConfig
import com.uploader.container.TestDatabase
import io.ktor.server.netty.NettyApplicationEngine
import java.io.Closeable
import java.io.File
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class TestApp(environment: String = "dev") : Closeable, KoinComponent {
    private val database: TestDatabase = TestDatabase()
    private val server: NettyApplicationEngine = App(environment).start()

    private val config by inject<AppConfig>()

    override fun close() {
        val file = File(config.rootBuildsPath)
        file.deleteRecursively()

        database.close()
        server.stop(1000, 1000)
    }
}
