package com.uploader.spec

import com.uploader.DatabaseTool.doInitialSetup
import com.uploader.MockedHttp
import com.uploader.TestApp
import com.uploader.TestingConstants.appUrl
import com.uploader.TestingTool.downloadFromResource
import com.uploader.config.AppConfig
import com.uploader.dao.dto.BuildDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.util.toByteArray
import kotlinx.coroutines.runBlocking
import net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.joda.time.format.DateTimeFormatter
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.KoinTest

@KoinApiExtension
class StatusResourceSpec : KoinTest {
    private val formatter by inject<DateTimeFormatter>()

    private val client = HttpClient()

    private lateinit var config: AppConfig
    private lateinit var app: TestApp
    private lateinit var builds: List<BuildDto>

    @BeforeEach
    fun setup() {
        app = TestApp("test")
        config = app.config
        loadKoinModules(module { single(override = true) { MockedHttp.client } })

        builds = doInitialSetup()
    }

    @Test
    fun `should return html with app activity info`() {
        // when
        val response = runBlocking { client.get<HttpResponse>("${config.appUrl()}/") }
        val actual = runBlocking { response.content.toByteArray().decodeToString() }

        // then
        assertThat(response.status, equalTo(OK))

        val expected = downloadFromResource("resource/app_activity_info_html").decodeToString()
        assertThat(actual, equalTo(withSubstitutedDates(expected)))
    }

    @Test
    fun `should return json with app activity info`() {
        // when
        val response = runBlocking { client.get<HttpResponse>("${config.appUrl()}/status") }
        val data = runBlocking { response.content.toByteArray().decodeToString() }

        // then
        assertThat(response.status, equalTo(OK))

        val expected = downloadFromResource("resource/app_activity_info_json.json").decodeToString()
        assertJsonEquals(data, withSubstitutedDates(expected))
    }

    private fun withSubstitutedDates(expected: String) =
        builds.fold(expected) { acc, build ->
            val created = formatter.print(build.dateCreated)
            val updated = formatter.print(build.dateUpdated)
            val fullNumber = build.fullNumber

            val withSubstution = acc.replace("{Created_$fullNumber}", created)
                .replace("{Updated_$fullNumber}", updated)

            withSubstution
        }

    @AfterEach
    fun close() {
        app.close()
        MockedHttp.reset()
    }
}
