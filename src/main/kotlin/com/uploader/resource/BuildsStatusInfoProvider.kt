package com.uploader.resource

import com.uploader.dao.dto.BuildDto.State
import com.uploader.dao.repository.BuildRepository
import com.uploader.db.DatabaseProvider
import kotlinx.coroutines.runBlocking
import org.joda.time.format.DateTimeFormatter
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class BuildsStatusInfoProvider : KoinComponent {
    private val buildRepository by inject<BuildRepository>()
    private val provider by inject<DatabaseProvider>()
    private val formatter by inject<DateTimeFormatter>()

    fun provide(): List<BuildStatusInfo> =
        runBlocking {
            provider.dbQuery {
                buildRepository.gelAllWithStates(State.values().toList())
                    .map { buildDto ->
                        BuildStatusInfo(
                            productName = buildDto.productName,
                            fullNumber = buildDto.fullNumber,
                            state = buildDto.state.name,
                            dateCreated = formatter.print(buildDto.dateCreated),
                            dateUpdated = formatter.print(buildDto.dateUpdated)
                        )
                    }
            }
        }

    data class BuildStatusInfo(
        val productName: String,
        val fullNumber: String,
        val state: String,
        val dateCreated: String,
        val dateUpdated: String
    )
}
