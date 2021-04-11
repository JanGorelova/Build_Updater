package com.uploader.dao.repository

import com.fasterxml.jackson.databind.JsonNode
import com.uploader.dao.dto.BuildInfoDto

interface BuildInfoRepository {
    suspend fun insert(buildInfoDto: BuildInfoDto)

    suspend fun findByBuildId(buildId: Int): BuildInfoDto?

    suspend fun findAllByProductName(productName: String): Map<String, JsonNode>

    suspend fun findByProductNameAndBuildNumber(productName: String, fullNumber: String): JsonNode?
}
