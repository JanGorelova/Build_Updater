package com.uploader

import com.uploader.container.TestDatabase
import io.ktor.client.HttpClient
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject
import org.koin.test.KoinTest

@KoinApiExtension
class ProductInfoRefresherResourceSpec : KoinTest {
    private val client by inject<HttpClient>()

    private lateinit var app: TestApp
    private lateinit var db: TestDatabase

    @BeforeEach
    fun setup() {
        db = TestDatabase()
        app = TestApp()
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
