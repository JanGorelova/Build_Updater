package com.uploader.dao.repository

import com.uploader.dao.dto.BuildDto
import com.uploader.dao.dto.BuildDto.State.DOWNLOADED
import com.uploader.dao.dto.BuildDto.State.PROCESSING
import com.uploader.dao.entity.Build
import com.uploader.dao.entity.Build.id
import com.uploader.dao.entity.Build.state
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateStatement

class BuildRepositoryImpl : BuildRepository {
    override fun insert(buildDto: BuildDto): Int =
        Build.insert {
            it[fullNumber] = buildDto.fullNumber
            it[channelId] = buildDto.channelId
            it[version] = buildDto.version
            it[state] = buildDto.state.name
            it[productName] = buildDto.productName
            it[productCode] = buildDto.productCode
        }[id].value

    override fun gelAllWithStates(states: List<BuildDto.State>) =
        Build.select {
            state.inList(states.map { it.name })
        }.map { it.mapToBuild() }

    override fun getBy(fullNumber: String, channelId: String): BuildDto? =
        Build.select {
            Build.fullNumber.eq(fullNumber).and { Build.channelId.eq(channelId) }
        }
            .mapNotNull { it.mapToBuild() }
            .singleOrNull()

    override fun processing(id: Int, previousState: BuildDto.State) {
        update(
            where = { Build.id.eq(Build.id).and(state.eq(previousState.name)) },
            update = { it[state] = PROCESSING.name }
        )
    }

    override fun failed(id: Int, previousState: BuildDto.State) {
        update(
            where = { Build.id.eq(Build.id).and(state.eq(previousState.name)) },
            update = { it[state] = BuildDto.State.FAILED.name }
        )
    }

    override fun downloaded(id: Int, previousState: BuildDto.State, path: String) {
        update(
            where = { Build.id.eq(Build.id).and(state.eq(previousState.name)) },
            update = {
                it[state] = DOWNLOADED.name
                it[Build.path] = path
            }
        )
    }

    private fun update(where: (SqlExpressionBuilder.() -> Op<Boolean>), update: Build.(UpdateStatement) -> Unit) {
        val updatedCount = Build.update(where = where, body = update)

        if (updatedCount == 0)
            throw RuntimeException("Could not update build record with id $id to $update")
    }

    private fun ResultRow.mapToBuild() =
        BuildDto(
            id = this[id].value,
            fullNumber = this[Build.fullNumber],
            channelId = this[Build.channelId],
            state = BuildDto.State.valueOf(this[state]),
            version = this[Build.version],
            productCode = this[Build.productCode],
            dateCreated = this[Build.dateCreated].toLocalDateTime(),
            productName = this[Build.productName]
        )
}