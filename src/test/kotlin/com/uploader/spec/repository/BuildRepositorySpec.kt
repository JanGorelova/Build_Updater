package com.uploader.spec.repository

import com.uploader.TestApp
import com.uploader.TestingData.py1BuildDto
import com.uploader.TestingData.py2BuildDto
import com.uploader.TestingData.wsBuildDto
import com.uploader.dao.dto.BuildDto
import com.uploader.dao.dto.BuildDto.State.CREATED
import com.uploader.dao.dto.BuildDto.State.DOWNLOADED
import com.uploader.dao.dto.BuildDto.State.FAILED
import com.uploader.dao.dto.BuildDto.State.PROCESSING
import com.uploader.dao.repository.BuildRepository
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject
import org.koin.test.KoinTest

@KoinApiExtension
class BuildRepositorySpec : KoinTest {
    private val buildRepository by inject<BuildRepository>()

    private lateinit var app: TestApp

    @BeforeEach
    fun setup() {
        app = TestApp("testWithoutJobs")
    }

    @Test
    fun `should return dto by full number and channel`() {
        // given
        val toSave = py1BuildDto
        val id = runBlocking { buildRepository.insert(toSave) }

        // when
        val actual = runBlocking {
            buildRepository.getByFullNumberAndChannel(
                toSave.fullNumber,
                toSave.channelId
            )
        } ?: error("Build does not exist")

        // then
        val expected = toSave.copy(
            id = id,
            dateCreated = actual.dateCreated,
            dateUpdated = actual.dateUpdated
        )

        assertThat(actual, equalTo(expected))
    }

    @Test
    fun `should return dtos with specified states after updates`() {
        // given
        val py1Dto = py1BuildDto
        val py2Dto = py2BuildDto
        val wsDto = wsBuildDto
        val py1Id = runBlocking { buildRepository.insert(py1Dto) }
        val py2Id = runBlocking { buildRepository.insert(py2Dto) }
        val wsId = runBlocking { buildRepository.insert(wsDto) }

        runBlocking {
            buildRepository.processing(py1Id, CREATED)
            buildRepository.processing(py2Id, CREATED)
            buildRepository.processing(wsId, CREATED)
        }

        val path = "test/path"
        runBlocking {
            buildRepository.failed(py1Id, PROCESSING)
            buildRepository.downloaded(wsId, PROCESSING, "test/path")
        }

        // when
        val actuals = runBlocking {
            buildRepository.gelAllWithStates(listOf(DOWNLOADED, FAILED, PROCESSING))
        }

        // then
        assertThat(actuals, hasSize(3))

        val currentPy1 = actuals.first { it.fullNumber == py1Dto.fullNumber }
        val expectedPy1 = py1Dto.copy(
            id = py1Id,
            state = FAILED,
            dateCreated = currentPy1.dateCreated,
            dateUpdated = currentPy1.dateUpdated
        )

        val currentPy2 = actuals.first { it.fullNumber == py2Dto.fullNumber }
        val expectedPy2 = py2Dto.copy(
            id = py2Id,
            state = PROCESSING,
            dateCreated = currentPy2.dateCreated,
            dateUpdated = currentPy2.dateUpdated
        )

        val currentWs = actuals.first { it.fullNumber == wsDto.fullNumber }
        val expectedWs = wsDto.copy(
            id = wsId,
            state = DOWNLOADED,
            dateCreated = currentWs.dateCreated,
            dateUpdated = currentWs.dateUpdated,
            path = path
        )

        assertThat(currentPy1, equalTo(expectedPy1))
        assertThat(currentPy2, equalTo(expectedPy2))
        assertThat(currentWs, equalTo(expectedWs))
    }

    @MethodSource("previous states to applied changes for processing state change")
    @ParameterizedTest
    fun `should fail to change state to processing if record was updated in another thread`(
        previousState: BuildDto.State,
        changeAppliedInAnotherThread: BuildRepository.() -> Unit,
        expectedComment: String
    ) {
        // given
        val id = runBlocking { buildRepository.insert(py1BuildDto) }
        runBlocking {
            changeAppliedInAnotherThread.invoke(buildRepository)
        }

        // when
        val invocation: () -> Unit = {
            runBlocking {
                buildRepository.processing(id, previousState = previousState)
            }
        }

        // then
        val error = assertThrows<RuntimeException>(invocation)
        val expected = RuntimeException("Could not update build record with id: 1, comment: $expectedComment")

        assertThat(error.message, equalTo(expected.message))
    }

    @MethodSource("applied changes for not allowed previous states")
    @ParameterizedTest
    fun `should fail if not allowed previous state is used`(
        changeToApply: BuildRepository.() -> Unit,
        expectedMessage: String
    ) {
        // given
        runBlocking { buildRepository.insert(py1BuildDto) }

        // when
        val invocation: () -> Unit = {
            runBlocking {
                changeToApply.invoke(buildRepository)
            }
        }

        // then
        val error = assertThrows<RuntimeException>(invocation)
        val expected = RuntimeException(expectedMessage)

        assertThat(error.message, equalTo(expected.message))
    }

    @Test
    fun `should fail if build was marked as failed from another thread`() {
        // given
        val id = runBlocking { buildRepository.insert(py1BuildDto) }
        runBlocking {
            buildRepository.processing(id, CREATED)
            buildRepository.failed(id, PROCESSING)
        }

        // when
        val invocation: () -> Unit = {
            runBlocking {
                buildRepository.failed(id, previousState = PROCESSING)
            }
        }

        // then
        val error = assertThrows<RuntimeException>(invocation)
        val expectedMessage =
            "Could not update build record with id: 1, comment: from PROCESSING to FAILED, current state is: FAILED"

        assertThat(error.message, equalTo(expectedMessage))
    }

    @Test
    fun `should fail if build was marked as downloaded from another thread`() {
        // given
        val id = runBlocking { buildRepository.insert(py1BuildDto) }
        runBlocking {
            buildRepository.processing(id, CREATED)
            buildRepository.downloaded(id, PROCESSING, "test/path")
        }

        // when
        val invocation: () -> Unit = {
            runBlocking {
                buildRepository.downloaded(id, previousState = PROCESSING, "test1/path")
            }
        }

        // then
        val error = assertThrows<RuntimeException>(invocation)
        val expectedMessage =
            "Could not update build record with id: 1, comment: from PROCESSING to DOWNLOADED, current state is: DOWNLOADED"

        assertThat(error.message, equalTo(expectedMessage))
    }

    @AfterEach
    fun close() {
        app.close()
    }

    private companion object {
        @JvmStatic
        fun `previous states to applied changes for processing state change`() = listOf(
            arguments(
                CREATED,
                { b: BuildRepository -> runBlocking { b.processing(1, CREATED) } },
                "from CREATED to PROCESSING, current state is: PROCESSING"
            ),
            arguments(
                FAILED,
                { b: BuildRepository -> runBlocking { b.processing(1, CREATED) } },
                "from FAILED to PROCESSING, current state is: PROCESSING"
            )
        )

        @JvmStatic
        fun `applied changes for not allowed previous states`() = listOf(
            arguments(
                { b: BuildRepository -> runBlocking { b.processing(1, DOWNLOADED) } },
                "Only [CREATED, FAILED] previous states are allowed"
            ),
            arguments(
                { b: BuildRepository -> runBlocking { b.processing(1, PROCESSING) } },
                "Only [CREATED, FAILED] previous states are allowed"
            ),
            arguments(
                { b: BuildRepository -> runBlocking { b.downloaded(1, CREATED, "test/path") } },
                "Only [PROCESSING] previous states are allowed"
            ),
            arguments(
                { b: BuildRepository -> runBlocking { b.downloaded(1, FAILED, "test/path1") } },
                "Only [PROCESSING] previous states are allowed"
            ),
            arguments(
                { b: BuildRepository -> runBlocking { b.failed(1, DOWNLOADED) } },
                "Only [PROCESSING] previous states are allowed"
            ),
            arguments(
                { b: BuildRepository -> runBlocking { b.failed(1, CREATED) } },
                "Only [PROCESSING] previous states are allowed"
            )
        )
    }
}
