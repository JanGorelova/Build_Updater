package com.uploader.unit.resource

import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.uploader.TestingConstants.PYCHARM_2_EXPECTED_INFO_JSON
import com.uploader.TestingConstants.PYCHARM_2_FULL_NUMBER
import com.uploader.TestingConstants.PYCHARM_EXPECTED_INFO_JSON
import com.uploader.TestingConstants.PYCHARM_FULL_NUMBER
import com.uploader.TestingConstants.WEBSTORM_EXPECTED_INFO_JSON
import com.uploader.TestingConstants.WEBSTORM_FULL_NUMBER
import com.uploader.TestingTool.downloadFromResource
import com.uploader.dao.repository.BuildInfoRepository
import com.uploader.lifecycle.Constants.PYCHARM
import com.uploader.lifecycle.Constants.WEBSTORM
import com.uploader.resource.ProductBuildsProvider
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.component.KoinApiExtension
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyZeroInteractions

@KoinApiExtension
class ProductBuildsProviderTest : KoinTest {
    private lateinit var buildInfoRepository: BuildInfoRepository

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(
            module {
                single { buildInfoRepository }
            }
        )
    }

    @BeforeEach
    fun setup() {
        buildInfoRepository = mock()
    }

    @Test
    fun `should provide infos by product`() {
        // given
        val mapper = jsonMapper()
        val expected = mapOf(
            PYCHARM_FULL_NUMBER to mapper.readTree(downloadFromResource(PYCHARM_EXPECTED_INFO_JSON)),
            PYCHARM_2_FULL_NUMBER to mapper.readTree(downloadFromResource(PYCHARM_2_EXPECTED_INFO_JSON))
        )
        buildInfoRepository.stub {
            onBlocking { findAllByProductName(PYCHARM) }.thenReturn(expected)
        }

        // when
        val result = runBlocking { ProductBuildsProvider().provideByProduct("PCA") }

        // then
        assertThat(result, equalTo(expected))
    }

    @Test
    fun `should fail if unknown product code provided`() {
        // when
        val invocation: () -> Unit = { runBlocking { ProductBuildsProvider().provideByProduct("UNKNOWN") } }

        // then
        assertThrows<IllegalStateException>(invocation)
        verifyZeroInteractions(buildInfoRepository)
    }

    @Test
    fun `should fail if info was not found`() {
        // given
        buildInfoRepository.stub {
            onBlocking { findByProductNameAndBuildNumber(WEBSTORM, WEBSTORM_FULL_NUMBER) }.thenReturn(null)
        }

        // when
        val invocation: () -> Unit = {
            runBlocking {
                ProductBuildsProvider().provideByProductCodeAndBuild("WS", WEBSTORM_FULL_NUMBER)
            }
        }

        // then
        val error = assertThrows<IllegalStateException>(invocation)
        runBlocking {
            verify(buildInfoRepository)
                .findByProductNameAndBuildNumber(WEBSTORM, WEBSTORM_FULL_NUMBER)
        }
        assertThat(error.message, equalTo("Specified build: 211.6693.108 was not found"))
    }

    @Test
    fun `should fail to provide by code and build if unknown product code provided`() {
        // given

        // when
        val invocation: () -> Unit =
            { runBlocking { ProductBuildsProvider().provideByProductCodeAndBuild("UNKNOWN", "Build_number") } }

        // then
        assertThrows<IllegalStateException>(invocation)
        verifyZeroInteractions(buildInfoRepository)
    }

    @Test
    fun `should provide infos by full number`() {
        // given
        val mapper = jsonMapper()
        val expected = mapper.readTree(downloadFromResource(WEBSTORM_EXPECTED_INFO_JSON))
        buildInfoRepository.stub {
            onBlocking { findByProductNameAndBuildNumber(WEBSTORM, WEBSTORM_FULL_NUMBER) }.thenReturn(expected)
        }

        // when
        val result = runBlocking {
            ProductBuildsProvider().provideByProductCodeAndBuild("WS", WEBSTORM_FULL_NUMBER)
        }

        // then
        assertThat(result, equalTo(expected))
    }
}
