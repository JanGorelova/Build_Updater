package com.uploader.dao.repository

import com.fasterxml.jackson.databind.JsonNode
import com.uploader.dao.dto.BuildInfoDto

interface BuildInfoRepository {
    fun insert(buildInfoDto: BuildInfoDto)

    fun findByBuildId(buildId: Int): BuildInfoDto?

    fun findAllByProductName(productName: String): Map<String, JsonNode>

    fun findByProductNameAndBuildNumber(productCode: String, fullNumber: String): JsonNode
}
