package com.uploader.unit.lifecycle

import com.uploader.TestingTool.getResourceFullPath
import com.uploader.dao.repository.BuildRepository
import com.uploader.lifecycle.ChecksumVerifier
import java.io.File
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.koin.core.component.KoinApiExtension
import org.mockito.kotlin.mock

@KoinApiExtension
class ChecksumVerifierTest {
    private lateinit var buildRepository: BuildRepository

    private val file = File(getResourceFullPath("app/tars/webstorm.tar.gz"))

    @BeforeEach
    fun setup() {
        buildRepository = mock()
    }

    @MethodSource("expected checksum to integrity value")
    @ParameterizedTest
    fun `should verify integral file`(
        expectedCheckSum: String,
        expectedResult: Boolean
    ) {
        // when
        val isIntegral = runBlocking { ChecksumVerifier().isIntegral(file, expectedCheckSum) }

        // then
        assertEquals(expectedResult, isIntegral)
    }

    private companion object {
        @JvmStatic
        fun `expected checksum to integrity value`() = listOf(
            arguments("483fbcf0b2cc8df86ac47e26a0022dd8a2f064d15811e814dc6fc83b0e1fbc91", true),
            arguments("483fbcf0b2cc8df86ac47e26a0022dd8a2f064d15811e814dc6fc83b0e1fbc94", false)
        )
    }
}
