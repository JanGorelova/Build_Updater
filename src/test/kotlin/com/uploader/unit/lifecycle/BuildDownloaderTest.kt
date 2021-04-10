package com.uploader.unit.lifecycle

import com.uploader.TestConfigProvider.appConfig
import com.uploader.TestingData.wsBuildDto
import com.uploader.TestingTool.getResourceFullPath
import com.uploader.config.AppConfig
import com.uploader.dao.dto.BuildDto.State.CREATED
import com.uploader.dao.dto.BuildDto.State.PROCESSING
import com.uploader.dao.repository.BuildRepository
import com.uploader.lifecycle.BuildDownloader
import com.uploader.lifecycle.ChecksumVerifier
import com.uploader.lifecycle.DownloadInfoGenerator
import com.uploader.lifecycle.DownloadInfoGenerator.DownloadInfo
import com.uploader.lifecycle.FileHelper
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.features.HttpTimeout
import io.ktor.utils.io.ByteReadChannel
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking
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
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever

@KoinApiExtension
class BuildDownloaderTest : KoinTest {
    private lateinit var downloadInfoGenerator: DownloadInfoGenerator
    private lateinit var buildRepository: BuildRepository
    private lateinit var verifier: ChecksumVerifier
    private lateinit var fileHelper: FileHelper
    private lateinit var client: HttpClient
    private lateinit var config: AppConfig
    private lateinit var clientCallNumber: AtomicInteger

    private val link = "test/download/link"
    private val id = 1
    private val file = File(getResourceFullPath("temp/temporary.tar.gz"))

    private val downloadInfo = DownloadInfo(
        downloadLink = link,
        checkSum = "384090390ganu45"
    )
    private val filePath = "test/path/file.tar.gz"
    private val directory = "test/path"
    private val buildDto = wsBuildDto.copy(id = id)

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(
            module {
                single { downloadInfoGenerator }
                single { client }
                single { buildRepository }
                single { verifier }
                single { config }
                single { fileHelper }
            }
        )
    }

    @BeforeEach
    fun setup() {
        downloadInfoGenerator = mock()
        buildRepository = mock()
        verifier = mock()
        config = appConfig()
        fileHelper = mock()
        clientCallNumber = AtomicInteger(0)
        client = httpMock()
    }

    @Test
    fun `should download build successfully`() {
        // given
        val downloadInfo = DownloadInfo(
            downloadLink = link,
            checkSum = "384090390ganu45"
        )

        val filePath = "test/path/file.tar.gz"
        val directory = "test/path"
        val buildDto = wsBuildDto.copy(id = id)
        whenever(downloadInfoGenerator[buildDto]).thenReturn(downloadInfo)
        whenever(fileHelper.directoryAndFilePath(buildDto)).thenReturn(directory to filePath)
        whenever(fileHelper.alreadyExists(filePath, checkSum = downloadInfo.checkSum)).thenReturn(false)

        whenever(fileHelper.createFile(directory, filePath)).thenReturn(file)
        whenever(verifier.isIntegral(file, downloadInfo.checkSum)).thenReturn(true)

        // when
        runBlocking { BuildDownloader().download(wsBuildDto.copy(id = id)) }

        // then
        runBlocking {
            verify(buildRepository).processing(id, CREATED)
            verify(buildRepository).downloaded(id, PROCESSING, filePath)
        }
        assertEquals(1, clientCallNumber.get())
    }

    @Test
    fun `should not download if already exists`() {
        // given
        whenever(downloadInfoGenerator[buildDto]).thenReturn(downloadInfo)
        whenever(fileHelper.directoryAndFilePath(buildDto)).thenReturn(directory to filePath)
        whenever(fileHelper.alreadyExists(filePath, checkSum = downloadInfo.checkSum)).thenReturn(true)

        // when
        runBlocking { BuildDownloader().download(wsBuildDto.copy(id = id)) }

        // then
        runBlocking {
            verify(buildRepository).processing(id, CREATED)
            verify(buildRepository).downloaded(id, PROCESSING, filePath)
            verifyZeroInteractions(verifier)
        }
        assertEquals(0, clientCallNumber.get())
    }

    @Test
    fun `should fail to download if exception thrown`() {
        // given
        whenever(downloadInfoGenerator[buildDto]).thenReturn(downloadInfo)
        whenever(fileHelper.directoryAndFilePath(buildDto))
            .thenThrow(RuntimeException("Something went wrong!"))

        // when
        runBlocking { BuildDownloader().download(wsBuildDto.copy(id = id)) }

        // then
        runBlocking {
            verify(buildRepository).processing(id, CREATED)
            verify(buildRepository).failed(id, PROCESSING)
            verifyZeroInteractions(verifier)
        }
        assertEquals(0, clientCallNumber.get())
    }

    @Test
    fun `should do nothing if another thread already processing current build`() {
        // given
        buildRepository.stub {
            onBlocking { processing(id, CREATED) }.thenThrow(RuntimeException("Could not update!"))
        }

        // when
        val invocation: () -> Unit = { runBlocking { BuildDownloader().download(wsBuildDto.copy(id = id)) } }

        // then
        assertThrows<RuntimeException>(invocation)
        runBlocking {
            verify(buildRepository).processing(id, CREATED)
            verifyNoMoreInteractions(buildRepository)
            verifyZeroInteractions(verifier)
            verifyZeroInteractions(fileHelper)
        }
        assertEquals(0, clientCallNumber.get())
    }

    private fun httpMock() = HttpClient(MockEngine) {
        install(HttpTimeout) { requestTimeoutMillis = 10000 }
        engine {
            addHandler { request ->
                when (request.url.encodedPath) {
                    "/$link" -> respond(ByteReadChannel("test content")).also { clientCallNumber.incrementAndGet() }
                    else -> error("Unknown url")
                }
            }
        }
    }
}
