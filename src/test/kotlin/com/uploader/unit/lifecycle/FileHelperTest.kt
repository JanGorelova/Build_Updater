package com.uploader.unit.lifecycle

import com.uploader.TestConfigProvider.appConfig
import com.uploader.TestingData.py2BuildDto
import com.uploader.TestingTool.getResourceFullPath
import com.uploader.lifecycle.ChecksumVerifier
import com.uploader.lifecycle.FileHelper
import java.io.File
import java.nio.file.Paths
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.component.KoinApiExtension
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyZeroInteractions
import org.mockito.kotlin.whenever

@KoinApiExtension
class FileHelperTest : KoinTest {
    private lateinit var checksumVerifier: ChecksumVerifier
    private val config = appConfig()

    private val checksum = "test_expected"

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(
            module {
                single { checksumVerifier }
                single { config }
            }
        )
    }

    @BeforeEach
    fun setup() {
        checksumVerifier = mock()
    }

    @Test
    fun `should verify that file already exists`() {
        // given
        val path = getResourceFullPath("app/tars/webstorm.tar.gz")
        val file = File(path)
        whenever(checksumVerifier.isIntegral(file, checksum)).thenReturn(true)

        // when
        val result = FileHelper().alreadyExists(path, checksum)

        // then
        assertTrue(result)
    }

    @Test
    fun `should remove file if not integral`() {
        // given
        val path = Paths.get("").toAbsolutePath()
        val file = File("$path/src/test/resources/temp/test.tar.gz")
        file.createNewFile()

        whenever(checksumVerifier.isIntegral(file, checksum)).thenReturn(false)

        // when
        val result = FileHelper().alreadyExists(file.path, checksum)

        // then
        assertFalse(result)
        assertFalse(file.exists())
    }

    @Test
    fun `should verify file does not exist`() {
        // when
        val result = FileHelper().alreadyExists("/unknown/test.tar.gz", checksum)

        // then
        assertFalse(result)
        verifyZeroInteractions(checksumVerifier)
    }

    @Test
    fun `should return directory and file path for build tar`() {
        // when
        val (dir, path) = FileHelper().directoryAndFilePath(py2BuildDto)

        // then
        val expectedDir = "${config.rootBuildsPath}${py2BuildDto.productName}"
        val expectedPath = "$expectedDir/${py2BuildDto.fullNumber}.tar.gz"

        assertThat(dir, equalTo(expectedDir))
        assertThat(path, equalTo(expectedPath))
    }

    @Test
    fun `should create dirs and file`() {
        // given
        val path = Paths.get("").toAbsolutePath()
        val directory = "$path/src/test/resources/new/test/dir"
        val filePath = "$directory/test-created.tar.gz"

        // when
        val result = FileHelper().createFile(directory, filePath)

        // then
        assertTrue(result.exists())
    }
}
