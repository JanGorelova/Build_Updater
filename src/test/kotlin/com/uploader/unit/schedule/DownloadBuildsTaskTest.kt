package com.uploader.unit.schedule

import com.uploader.TestingData.py1BuildDto
import com.uploader.TestingData.wsBuildDto
import com.uploader.dao.dto.BuildDto.State.CREATED
import com.uploader.dao.dto.BuildDto.State.FAILED
import com.uploader.dao.repository.BuildRepository
import com.uploader.lifecycle.BuildDownloader
import com.uploader.schedule.DownloadBuildsTask
import kotlinx.coroutines.runBlocking
import org.awaitility.Awaitility.await
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
class DownloadBuildsTaskTest : KoinTest {
    private lateinit var buildRepository: BuildRepository
    private lateinit var buildDownloader: BuildDownloader

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(
            module {
                single { buildDownloader }
                single { buildRepository }
            }
        )
    }

    @BeforeEach
    fun setup() {
        buildRepository = mock()
        buildDownloader = mock()
    }

    @Test
    fun `should start download process`() {
        // given
        buildRepository.stub {
            onBlocking { gelAllWithStates(listOf(CREATED, FAILED)) }
                .thenReturn(listOf(py1BuildDto, wsBuildDto))
        }

        // when
        DownloadBuildsTask().run()

        // then
        await().untilAsserted {
            runBlocking {
                verify(buildDownloader).download(py1BuildDto)
                verify(buildDownloader).download(wsBuildDto)
                verifyNoMoreInteractions(buildDownloader)
            }
        }
    }
}
