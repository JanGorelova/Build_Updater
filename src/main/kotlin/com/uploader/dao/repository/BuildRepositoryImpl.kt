package com.uploader.dao.repository

import com.uploader.dao.dto.BuildDto
import com.uploader.dao.dto.BuildDto.State
import com.uploader.dao.dto.BuildDto.State.CREATED
import com.uploader.dao.dto.BuildDto.State.DOWNLOADED
import com.uploader.dao.dto.BuildDto.State.FAILED
import com.uploader.dao.dto.BuildDto.State.PROCESSING
import com.uploader.dao.entity.Build
import com.uploader.dao.entity.Build.id
import com.uploader.dao.entity.Build.state
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.jetbrains.exposed.sql.update
import org.joda.time.DateTime

class BuildRepositoryImpl : BuildRepository {
    override fun insert(buildDto: BuildDto): Int =
        Build.insert {
            it[fullNumber] = buildDto.fullNumber
            it[channelId] = buildDto.channelId
            it[version] = buildDto.version
            it[state] = CREATED.name
            it[productName] = buildDto.productName
            it[productCode] = buildDto.productCode
        }[id].value

    override fun gelAllWithStates(states: List<State>) =
        Build.select {
            state.inList(states.map { it.name })
        }.map { it.mapToDto() }

    override fun getBy(fullNumber: String, channelId: String): BuildDto? =
        Build.select {
            Build.fullNumber.eq(fullNumber).and { Build.channelId.eq(channelId) }
        }
            .mapNotNull { it.mapToDto() }
            .singleOrNull()

    override fun processing(id: Int, previousState: State) {
        update(
            where = { Build.id.eq(id).and(state.eq(previousState.name)) },
            update = {
                it[state] = PROCESSING.name
                it[dateUpdated] = DateTime.now()
            }
        )
    }

    override fun failed(id: Int, previousState: State) {
        update(
            where = { Build.id.eq(id).and(state.eq(previousState.name)) },
            update = {
                it[state] = FAILED.name
                it[dateUpdated] = DateTime.now()
            }
        )
    }

    override fun downloaded(id: Int, previousState: State, path: String) {
        update(
            where = { Build.id.eq(id).and(state.eq(previousState.name)) },
            update = {
                it[state] = DOWNLOADED.name
                it[Build.path] = path
                it[dateUpdated] = DateTime.now()
            }
        )
    }

    private fun update(where: (SqlExpressionBuilder.() -> Op<Boolean>), update: Build.(UpdateStatement) -> Unit) {
        val updatedCount = Build.update(where = where, body = update)

        if (updatedCount == 0)
            throw RuntimeException("Could not update build record with id $id to $update")
    }

    private fun ResultRow.mapToDto() =
        BuildDto(
            id = this[id].value,
            fullNumber = this[Build.fullNumber],
            channelId = this[Build.channelId],
            state = State.valueOf(this[state]),
            version = this[Build.version],
            productCode = this[Build.productCode],
            dateCreated = this[Build.dateCreated].toLocalDateTime(),
            dateUpdated = this[Build.dateUpdated].toLocalDateTime(),
            productName = this[Build.productName],
            path = this[Build.path]
        )
}
