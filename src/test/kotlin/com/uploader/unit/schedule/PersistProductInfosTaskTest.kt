package com.uploader.unit.schedule

import com.uploader.TestingData.py1BuildDto
import com.uploader.TestingData.py2BuildDto
import com.uploader.TestingData.wsBuildDto
import com.uploader.dao.dto.BuildDto.State.DOWNLOADED
import com.uploader.dao.repository.BuildRepository
import com.uploader.lifecycle.BuildInfoPersister
import com.uploader.schedule.PersistProductInfosTask
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
class PersistProductInfosTaskTest : KoinTest {
    private lateinit var buildRepository: BuildRepository
    private lateinit var buildInfoPersister: BuildInfoPersister

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(
            module {
                single { buildInfoPersister }
                single { buildRepository }
            }
        )
    }

    @BeforeEach
    fun setup() {
        buildRepository = mock()
        buildInfoPersister = mock()
    }

    @Test
    fun `should persist product infos after build is downloaded`() {
        // given
        val downloadedPy1 = py1BuildDto.copy(id = 1, path = "/test/path", state = DOWNLOADED)
        val downloadedWs = wsBuildDto.copy(id = 2, path = "/test/path/test", state = DOWNLOADED)
        buildRepository.stub {
            onBlocking { gelAllWithStates(listOf(DOWNLOADED)) }
                .thenReturn(listOf(downloadedWs, downloadedPy1, py2BuildDto))
        }

        // when
        PersistProductInfosTask().run()

        // then
        await().untilAsserted {
            runBlocking {
                verify(buildInfoPersister).persistIfRequired(downloadedPy1.path!!, downloadedPy1.id!!)
                verify(buildInfoPersister).persistIfRequired(downloadedWs.path!!, downloadedWs.id!!)
                verifyNoMoreInteractions(buildInfoPersister)
            }
        }
    }
}
