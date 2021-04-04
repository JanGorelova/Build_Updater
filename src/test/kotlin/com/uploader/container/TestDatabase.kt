package com.uploader.container

import java.io.Closeable
import mu.KLogging
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.output.Slf4jLogConsumer

class TestDatabase : Closeable {
    private val db: PostgreSQLContainer<Db>

    init {
        db = Db()
        db.start()
    }

    private class Db : PostgreSQLContainer<Db>("postgres:11.1") {
        init {
            withDatabaseName("test_uploader")
                .withUsername("test")
                .withPassword("test")
                .withLogConsumer(Slf4jLogConsumer(logger))
                .withExposedPorts(5432)
                .addFixedExposedPort(5432, 5432)
        }
    }

    override fun close() {
        db.stop()
    }

    private companion object : KLogging()
}
