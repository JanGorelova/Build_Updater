package com.uploader.dao.repository

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.uploader.dao.dto.BuildInfoDto
import com.uploader.dao.entity.Build
import com.uploader.dao.entity.BuildInfo
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.JoinType.INNER
import org.jetbrains.exposed.sql.ResultRow
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

    override fun findAllByProductCode(productCode: String): Map<String, JsonNode> {
        val join = Join(
            Build, BuildInfo,
            onColumn = Build.id, otherColumn = BuildInfo.buildNumber,
            joinType = INNER,
            additionalConstraint = { Build.productCode eq productCode }
        )

        return join.selectAll()
            .map { it[Build.fullNumber] to jsonMapper.readTree(it[BuildInfo.product_info]) }
            .toMap()
    }

    override fun findByProductCodeAndBuildNumber(productCode: String, fullNumber: String): JsonNode {
        val join = Join(
            Build, BuildInfo,
            onColumn = Build.id, otherColumn = BuildInfo.buildNumber,
            joinType = INNER,
            additionalConstraint = { Build.productCode eq productCode }
        )

        return join.select { Build.fullNumber eq fullNumber }
            .map { jsonMapper.readTree(it[BuildInfo.product_info]) }
            .first()
    }

    private fun ResultRow.mapToDto() =
        BuildInfoDto(
            buildId = this[BuildInfo.buildNumber],
            info = this[BuildInfo.product_info]
        )
}
