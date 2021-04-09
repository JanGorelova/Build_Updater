package com.uploader.spec

import com.uploader.DatabaseTool.doInitialSetup
import com.uploader.MockedHttp
import com.uploader.TestApp
import com.uploader.TestingConstants.WEBSTORM_FULL_NUMBER
import com.uploader.TestingConstants.appUrl
import com.uploader.TestingTool.downloadFromResource
import com.uploader.config.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.util.toByteArray
import kotlinx.coroutines.runBlocking
import net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.component.KoinApiExtension
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.KoinTest

@KoinApiExtension
class ProductInfoResourceSpec : KoinTest {
    private val mockedHttp = MockedHttp()
    private val client = HttpClient()
    private lateinit var config: AppConfig

    private lateinit var app: TestApp

    @BeforeEach
    fun setup() {
        app = TestApp("test")
        config = app.config
        loadKoinModules(module { single(override = true) { mockedHttp.client } })

        doInitialSetup()
    }

    @Test
    fun `should return product info by product`() {
        // when
        val response = runBlocking { client.get<HttpResponse>("${config.appUrl()}/PY") }

        // then
        val actual = runBlocking { response.content.toByteArray().decodeToString() }
        assertThat(response.status, equalTo(OK))

        val expected = downloadFromResource("resource/py_product_infos.json").decodeToString()
        assertJsonEquals(expected, actual)
    }

    @Test
    fun `should return product info by product and build number`() {
        // when
        val response = runBlocking { client.get<HttpResponse>("${config.appUrl()}WS/$WEBSTORM_FULL_NUMBER") }

        // then
        val actual = runBlocking { response.content.toByteArray().decodeToString() }
        assertThat(response.status, equalTo(OK))

        val expected = downloadFromResource("app/tars/infos/webstorm-product-info.json").decodeToString()
        assertJsonEquals(expected, actual)
    }

    @AfterEach
    fun close() {
        app.close()
        mockedHttp.client.close()
    }
}
