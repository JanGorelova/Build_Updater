package com.uploader

import io.ktor.server.netty.NettyApplicationEngine
import java.io.Closeable
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class TestApp : Closeable {
    private val server: NettyApplicationEngine = App("dev").start()

    override fun close() {
        server.stop(1000, 1000)
    }
}
