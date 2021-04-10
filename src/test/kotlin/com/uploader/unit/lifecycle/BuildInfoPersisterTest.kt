package com.uploader.unit.lifecycle

import com.uploader.TestingData.buildInfoDto
import com.uploader.dao.repository.BuildInfoRepository
import com.uploader.lifecycle.BuildInfoPersister
import com.uploader.lifecycle.ProductInfoProvider
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.component.KoinApiExtension
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever

@KoinApiExtension
class BuildInfoPersisterTest : KoinTest {
    private lateinit var productInfoProvider: ProductInfoProvider
    private lateinit var buildInfoRepository: BuildInfoRepository

    private val id = 1
    private val filePath = "test/path/file.tar.gz"

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(
            module {
                single { buildInfoRepository }
                single { productInfoProvider }
            }
        )
    }

    @BeforeEach
    fun setup() {
        buildInfoRepository = mock()
        productInfoProvider = mock()
    }

    @Test
    fun `should persist info if does not exists`() {
        // given
        buildInfoRepository.stub {
            onBlocking { findByBuildId(id) }.thenReturn(null)
        }
        val info = """{"test":"data"}"""
        whenever(productInfoProvider.find(filePath)).thenReturn(info)

        // when
        runBlocking { BuildInfoPersister().persistIfRequired(filePath, id) }

        // then
        runBlocking {
            verify(buildInfoRepository).findByBuildId(id)
            verify(buildInfoRepository).insert(buildInfoDto(buildId = id, info = info))
        }
        verify(productInfoProvider).find(filePath)
    }

    @Test
    fun `should not persist info if info already exists`() {
        // given
        val info = buildInfoDto()
        buildInfoRepository.stub {
            onBlocking { findByBuildId(id) }.thenReturn(info)
        }

        // when
        runBlocking { BuildInfoPersister().persistIfRequired(filePath, id) }

        // then
        runBlocking {
            verify(buildInfoRepository).findByBuildId(id)
            verifyNoMoreInteractions(buildInfoRepository)
            verifyZeroInteractions(productInfoProvider)
        }
    }
}
