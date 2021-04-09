package com.uploader.container

import java.io.Closeable
import mu.KLogging
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait

class TestDatabase(port: Int) : Closeable {
    private val db: PostgreSQLContainer<Db>

    init {
        db = Db(port)
        db.start()
        db.waitingFor(Wait.forHealthcheck())
    }

    private class Db(port: Int) : PostgreSQLContainer<Db>("postgres:11.1") {
        init {
            withDatabaseName("test_uploader")
                .withUsername("test")
                .withPassword("test")
                .withLogConsumer(Slf4jLogConsumer(logger))
                .withExposedPorts(5432)
                .addFixedExposedPort(port, 5432)
        }
    }

    override fun close() {
        db.stop()
        db.close()
    }

    private companion object : KLogging()
}
