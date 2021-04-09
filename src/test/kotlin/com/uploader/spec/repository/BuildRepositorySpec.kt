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
import com.uploader.db.DatabaseProvider
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject
import org.koin.test.KoinTest

@KoinApiExtension
class BuildRepositorySpec : KoinTest {
    private val buildRepository by inject<BuildRepository>()
    private val databaseProvider by inject<DatabaseProvider>()

    private lateinit var app: TestApp

    @BeforeEach
    fun setup() {
        app = TestApp("testWithoutJobs")
    }

    @Test
    fun `should return dto by full number and channel`() {
        // given
        val toSave = py1BuildDto
        val id = runBlocking { databaseProvider.dbQuery { buildRepository.insert(toSave) } }

        // when
        val actual = runBlocking {
            databaseProvider.dbQuery {
                buildRepository.getByFullNumberAndChannel(
                    toSave.fullNumber,
                    toSave.channelId
                )
            }
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
        val py1Id = runBlocking { databaseProvider.dbQuery { buildRepository.insert(py1Dto) } }
        val py2Id = runBlocking { databaseProvider.dbQuery { buildRepository.insert(py2Dto) } }
        val wsId = runBlocking { databaseProvider.dbQuery { buildRepository.insert(wsDto) } }

        runBlocking {
            databaseProvider.dbQuery {
                buildRepository.processing(py1Id, CREATED)
                buildRepository.processing(py2Id, CREATED)
                buildRepository.processing(wsId, CREATED)
            }
        }

        val path = "test/path"
        runBlocking {
            databaseProvider.dbQuery {
                buildRepository.failed(py1Id, PROCESSING)
                buildRepository.downloaded(wsId, PROCESSING, "test/path")
            }
        }

        // when
        val actuals = runBlocking {
            databaseProvider.dbQuery {
                buildRepository.gelAllWithStates(listOf(DOWNLOADED, FAILED, PROCESSING))
            }
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

    @ParameterizedTest
    fun `should fail to change state to processing if record was updated in another thread`(
        previousState: BuildDto.State,
        changeAppliedInAnotherThread: BuildRepository.() -> Unit
    ) {
        // given
        changeAppliedInAnotherThread.apply { buildRepository }

        // when
        val invocation: () -> Unit = {
            runBlocking {
                databaseProvider.dbQuery { buildRepository.processing(1, previousState = previousState) }
            }
        }

        // then
        val error = assertThrows<RuntimeException>(invocation)
    }

    @AfterEach
    fun close() {
        app.close()
    }
}
