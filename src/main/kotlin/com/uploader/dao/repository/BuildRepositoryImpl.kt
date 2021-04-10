package com.uploader.dao.repository

import com.uploader.dao.dto.BuildDto
import com.uploader.dao.dto.BuildDto.State
import com.uploader.dao.dto.BuildDto.State.CREATED
import com.uploader.dao.dto.BuildDto.State.DOWNLOADED
import com.uploader.dao.dto.BuildDto.State.FAILED
import com.uploader.dao.dto.BuildDto.State.PROCESSING
import com.uploader.dao.entity.Build
import com.uploader.dao.entity.Build.id
import com.uploader.dao.entity.Build.path
import com.uploader.dao.entity.Build.state
import com.uploader.db.DatabaseProvider
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder.ASC
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.jetbrains.exposed.sql.update
import org.joda.time.DateTime
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class BuildRepositoryImpl : BuildRepository, KoinComponent {
    private val provider by inject<DatabaseProvider>()

    override suspend fun insert(buildDto: BuildDto): Int =
        provider.dbQuery {
            Build.insert {
                it[fullNumber] = buildDto.fullNumber
                it[channelId] = buildDto.channelId
                it[version] = buildDto.version
                it[state] = CREATED.name
                it[productName] = buildDto.productName
            }[id].value
        }

    override suspend fun gelAllWithStates(states: List<State>) =
        provider.dbQuery {
            Build.select { state.inList(states.map { it.name }) }
                .orderBy(Build.productName to ASC, Build.fullNumber to ASC)
                .map { it.mapToDto() }
        }

    override suspend fun getByFullNumberAndChannel(fullNumber: String, channelId: String): BuildDto? =
        provider.dbQuery {
            Build
                .select { Build.fullNumber.eq(fullNumber).and { Build.channelId.eq(channelId) } }
                .mapNotNull { it.mapToDto() }
                .singleOrNull()
        }

    override suspend fun processing(id: Int, previousState: State) {
        verifyPreviousState(previousState, allowedPreviousStatesForProcessing)

        provider.dbQuery {
            update(
                where = { Build.id.eq(id).and(state.eq(previousState.name)) },
                update = {
                    it[state] = PROCESSING.name
                    it[dateUpdated] = DateTime.now()
                },
                id = id,
                comment = "from $previousState to PROCESSING"
            )
        }
    }

    override suspend fun failed(id: Int, previousState: State) {
        verifyPreviousState(previousState, allowedPreviousStatesForFailed)

        provider.dbQuery {
            update(
                where = { Build.id.eq(id).and(state.eq(previousState.name)) },
                update = {
                    it[state] = FAILED.name
                    it[dateUpdated] = DateTime.now()
                },
                id = id,
                comment = "from $previousState to FAILED"
            )
        }
    }

    override suspend fun downloaded(id: Int, previousState: State, path: String) {
        verifyPreviousState(previousState, allowedPreviousStatesForDownloaded)

        provider.dbQuery {
            update(
                where = { Build.id.eq(id).and(state.eq(previousState.name)) },
                update = {
                    it[state] = DOWNLOADED.name
                    it[Build.path] = path
                    it[dateUpdated] = DateTime.now()
                },
                id = id,
                comment = "from $previousState to DOWNLOADED"
            )
        }
    }

    private fun update(
        where: (SqlExpressionBuilder.() -> Op<Boolean>),
        update: Build.(UpdateStatement) -> Unit,
        id: Int,
        comment: String
    ) {
        val updatedCount = Build.update(where = where, body = update)

        if (updatedCount == 0) {
            val currentState = Build.select { Build.id eq id }.first()[state]
            throw RuntimeException("Could not update build record with id: $id, comment: $comment, current state is: $currentState")
        }
    }

    private fun ResultRow.mapToDto() =
        BuildDto(
            id = this[id].value,
            fullNumber = this[Build.fullNumber],
            channelId = this[Build.channelId],
            state = State.valueOf(this[state]),
            version = this[Build.version],
            dateCreated = this[Build.dateCreated].toLocalDateTime(),
            dateUpdated = this[Build.dateUpdated].toLocalDateTime(),
            productName = this[Build.productName],
            path = this[path]
        )

    private fun verifyPreviousState(previousState: State, allowedPreviousStates: List<State>) {
        if (previousState !in allowedPreviousStates)
            throw RuntimeException("Only $allowedPreviousStates previous states are allowed")
    }

    private companion object {
        private val allowedPreviousStatesForFailed = listOf(PROCESSING)
        private val allowedPreviousStatesForProcessing = listOf(CREATED, FAILED)
        private val allowedPreviousStatesForDownloaded = listOf(PROCESSING)
    }
}
