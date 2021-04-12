package com.uploader.unit.lifecycle

import com.uploader.TestingData.buildUpdatesInfo
import com.uploader.TestingData.productsUpdates
import com.uploader.lifecycle.BuildInfoProvider
import com.uploader.module.XmlMapperProvider.xmlMapper
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.features.HttpTimeout
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.component.KoinApiExtension
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension

@KoinApiExtension
class BuildInfoProviderTest : KoinTest {
    private lateinit var client: HttpClient

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(
            module {
                single { client }
                single { xmlMapper() }
            }
        )
    }

    @BeforeEach
    fun setup() {
        client = clientMock()
    }

    @Test
    fun `should provide information about products`() {
        // when
        val updates = runBlocking { BuildInfoProvider().provide() }

        // then
        assertThat(updates, hasSize(5))
        assertThat(updates, equalTo(buildUpdatesInfo()))
    }

    private fun clientMock() = HttpClient(MockEngine) {
        install(HttpTimeout) { requestTimeoutMillis = 10000 }
        engine {
            addHandler { request ->
                when (request.url.encodedPath) {
                    "/updates/updates.xml" -> respond(productsUpdates())
                    else -> error("Unknown url")
                }
            }
        }
    }
}
