package com.uploader.dao.repository

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.uploader.dao.dto.BuildInfoDto
import com.uploader.dao.entity.Build
import com.uploader.dao.entity.Build.fullNumber
import com.uploader.dao.entity.BuildInfo
import com.uploader.dao.entity.BuildInfo.product_info
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.JoinType.INNER
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder.ASC
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class BuildInfoRepositoryImpl : BuildInfoRepository, KoinComponent {
    private val jsonMapper by inject<ObjectMapper>()

    override fun insert(buildInfoDto: BuildInfoDto) {
        BuildInfo.insert {
            it[buildNumber] = buildInfoDto.buildId
            it[product_info] = buildInfoDto.info
        }
    }

    override fun findByBuildId(buildId: Int) =
        BuildInfo.select {
            BuildInfo.buildNumber eq buildId
        }
            .mapNotNull { it.mapToDto() }
            .firstOrNull()

    override fun findAllByProductName(productName: String): Map<String, JsonNode> {
        val join = Join(
            Build, BuildInfo,
            onColumn = Build.id, otherColumn = BuildInfo.buildNumber,
            joinType = INNER,
            additionalConstraint = { Build.productName eq productName }
        )

        return join.selectAll()
            .orderBy(fullNumber, ASC)
            .map { it[fullNumber] to jsonMapper.readTree(it[product_info]) }
            .toMap()
    }

    override fun findByProductNameAndBuildNumber(productName: String, fullNumber: String): JsonNode {
        val join = Join(
            Build, BuildInfo,
            onColumn = Build.id, otherColumn = BuildInfo.buildNumber,
            joinType = INNER,
            additionalConstraint = { Build.productName eq productName }
        )

        return join.select { Build.fullNumber eq fullNumber }
            .map { jsonMapper.readTree(it[product_info]) }
            .first()
    }

    private fun ResultRow.mapToDto() =
        BuildInfoDto(
            buildId = this[BuildInfo.buildNumber],
            info = this[product_info]
        )
}
