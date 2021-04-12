package com.uploader.unit.lifecycle

import com.uploader.TestingData.ws1BuildUpdate
import com.uploader.TestingData.wsBuildDto
import com.uploader.dao.repository.BuildRepository
import com.uploader.lifecycle.BuildUpdatesPersister
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

@KoinApiExtension
class BuildUpdatesPersisterTest : KoinTest {
    private lateinit var buildRepository: BuildRepository

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(
            module {
                single { buildRepository }
            }
        )
    }

    @BeforeEach
    fun setup() {
        buildRepository = mock()
    }

    @Test
    fun `should persist build if does not exist`() {
        // given
        buildRepository.stub {
            onBlocking {
                getByFullNumberAndChannel(
                    ws1BuildUpdate.fullNumber,
                    ws1BuildUpdate.channelId
                )
            }.thenReturn(null)
        }

        // when
        runBlocking { BuildUpdatesPersister().saveBuildUpdateIfRequired(ws1BuildUpdate) }

        // then
        runBlocking {
            verify(buildRepository).getByFullNumberAndChannel(
                ws1BuildUpdate.fullNumber,
                ws1BuildUpdate.channelId
            )
            verify(buildRepository).insert(wsBuildDto)
        }
    }

    @Test
    fun `should not persist build if exists`() {
        // given
        buildRepository.stub {
            onBlocking {
                getByFullNumberAndChannel(
                    ws1BuildUpdate.fullNumber,
                    ws1BuildUpdate.channelId
                )
            }.thenReturn(wsBuildDto)
        }

        // when
        runBlocking { BuildUpdatesPersister().saveBuildUpdateIfRequired(ws1BuildUpdate) }

        // then
        runBlocking {
            verify(buildRepository).getByFullNumberAndChannel(
                ws1BuildUpdate.fullNumber,
                ws1BuildUpdate.channelId
            )
            verifyNoMoreInteractions(buildRepository)
        }
    }
}
