package com.uploader.unit.resource

import com.uploader.TestingData.py1BuildDto
import com.uploader.TestingData.wsBuildDto
import com.uploader.dao.dto.BuildDto.State
import com.uploader.dao.repository.BuildRepository
import com.uploader.resource.BuildsStatusInfoProvider
import com.uploader.resource.BuildsStatusInfoProvider.BuildStatusInfo
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.component.KoinApiExtension
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

@KoinApiExtension
class BuildsStatusInfoProviderTest : KoinTest {
    private lateinit var buildRepository: BuildRepository
    private lateinit var formatter: DateTimeFormatter

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(
            module {
                single { buildRepository }
                single { formatter }
            }
        )
    }

    @BeforeEach
    fun setup() {
        buildRepository = mock()
        formatter = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm")
    }

    @Test
    fun `should refresh released less than year ago`() {
        // given
        val date = LocalDateTime.now()
        val pyDtoSaved = py1BuildDto.copy(id = 1, dateCreated = date.minusDays(1), dateUpdated = date)
        val wsDtoSaved = wsBuildDto.copy(id = 2, dateCreated = date.minusHours(4), dateUpdated = date.minusHours(2))
        buildRepository.stub {
            onBlocking { gelAllWithStates(State.values().toList()) }
                .thenReturn(listOf(pyDtoSaved, wsDtoSaved))
        }

        // when
        val result = BuildsStatusInfoProvider().provide()

        // then
        val expected = listOf(
            BuildStatusInfo(
                productName = pyDtoSaved.productName,
                fullNumber = pyDtoSaved.fullNumber,
                state = pyDtoSaved.state.name,
                dateCreated = formatter.print(pyDtoSaved.dateCreated),
                dateUpdated = formatter.print(pyDtoSaved.dateUpdated)
            ),
            BuildStatusInfo(
                productName = wsDtoSaved.productName,
                fullNumber = wsDtoSaved.fullNumber,
                state = wsDtoSaved.state.name,
                dateCreated = formatter.print(wsDtoSaved.dateCreated),
                dateUpdated = formatter.print(wsDtoSaved.dateUpdated)
            )
        )

        assertThat(result, equalTo(expected))
    }
}
