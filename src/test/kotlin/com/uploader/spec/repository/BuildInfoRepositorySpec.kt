package com.uploader.spec.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.uploader.TestApp
import com.uploader.TestingData.buildInfoDto
import com.uploader.TestingData.py1BuildDto
import com.uploader.TestingData.py2BuildDto
import com.uploader.dao.repository.BuildInfoRepository
import com.uploader.dao.repository.BuildRepository
import kotlinx.coroutines.runBlocking
import net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject
import org.koin.test.KoinTest

@KoinApiExtension
class BuildInfoRepositorySpec : KoinTest {
    private val buildInfoRepository by inject<BuildInfoRepository>()
    private val buildRepository by inject<BuildRepository>()
    private val mapper by inject<ObjectMapper>()

    private lateinit var app: TestApp

    @BeforeEach
    fun setup() {
        app = TestApp("testWithoutRefreshJob")
    }

    @Test
    fun `should find build info by build id`() {
        // given
        val buildDto = py1BuildDto
        val buildId = runBlocking { buildRepository.insert(buildDto) }

        val buildInfoDto = buildInfoDto(
            buildId = buildId,
            info = "{test : info}"
        )
        runBlocking { buildInfoRepository.insert(buildInfoDto) }

        // when
        val actual = runBlocking { buildInfoRepository.findByBuildId(buildId) }

        // then
        assertThat(actual, equalTo(buildInfoDto))
    }

    @Test
    fun `should find all by product name`() {
        // given
        val py1Dto = py1BuildDto
        val py2Dto = py2BuildDto
        val py1Id = runBlocking { buildRepository.insert(py1BuildDto) }
        val py2Id = runBlocking { buildRepository.insert(py2BuildDto) }

        val py1buildInfoDto = buildInfoDto(
            buildId = py1Id,
            info = """
                {"test": "py1"}
            """.trimIndent()
        )
        val py2buildInfoDto = buildInfoDto(
            buildId = py2Id,
            info = """
                {"test": "py2"}
            """.trimIndent()
        )
        runBlocking { buildInfoRepository.insert(py1buildInfoDto) }
        runBlocking { buildInfoRepository.insert(py2buildInfoDto) }

        // when
        val actual = runBlocking {

            buildInfoRepository.findAllByProductName(py1Dto.productName)
        }

        // then
        val expected = mapOf(
            py1Dto.fullNumber to mapper.readTree(py1buildInfoDto.info),
            py2Dto.fullNumber to mapper.readTree(py2buildInfoDto.info)
        )

        assertThat(actual, equalTo(expected))
    }

    @Test
    fun `should find by product name and build number`() {
        // given
        val py1Dto = py1BuildDto
        val py1Id = runBlocking { buildRepository.insert(py1BuildDto) }

        val py1buildInfoDto = buildInfoDto(
            buildId = py1Id,
            info = """
                {"test": "py1"}
            """.trimIndent()
        )
        runBlocking { buildInfoRepository.insert(py1buildInfoDto) }

        // when
        val actual = runBlocking {
            buildInfoRepository.findByProductNameAndBuildNumber(py1Dto.productName, py1Dto.fullNumber)
        }

        // then
        assertJsonEquals(actual, mapper.readTree(py1buildInfoDto.info))
    }

    @AfterEach
    fun close() {
        app.close()
    }
}
