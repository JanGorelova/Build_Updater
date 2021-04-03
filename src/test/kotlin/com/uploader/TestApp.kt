package com.uploader

import io.ktor.server.netty.*
import io.ktor.util.*
import java.io.Closeable

@KtorExperimentalAPI
class TestApp : Closeable {
    private val server: NettyApplicationEngine = App("dev").start()

    override fun close() {
        server.stop(1000, 1000)
    }
}