package com.uploader

import com.uploader.container.TestDatabase
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.fullPath
import io.ktor.utils.io.ByteReadChannel
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.component.KoinApiExtension
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.KoinTest

@KoinApiExtension
class BuildUploadRaceConditionSpec : KoinTest {
    private lateinit var app: TestApp
    private lateinit var db: TestDatabase

    private val client: HttpClient = HttpClient(MockEngine) {
        engine {
            addHandler { request ->
                val path = request.url.toString()
                when {
                    path == "https://example.org/" -> {
                        respond(ByteReadChannel(file))
                    }
                    else -> error("Unhandled ${request.url.fullPath}")
                }
            }
        }
    }

    private val file = this::class.java
        .classLoader.getResourceAsStream("app/test-tar-go-lang.tar.gz")
        ?.readBytes() ?: error("")

    @BeforeEach
    fun setup() {
        db = TestDatabase()
        app = TestApp()
        loadKoinModules(module { single(override = true) { client } })
    }

    @Test
    fun test() {
        // given
        // create build info, start two threads at the same time and check on mock

        // when
        await()
            .atMost(200, TimeUnit.SECONDS)
            .untilAsserted {
                runBlocking { delay(10000000) }
            }

        // then
    }
}
