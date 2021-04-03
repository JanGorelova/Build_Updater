package com.uploader.dao.repository

import com.uploader.dao.dto.BuildInfoDto
import com.uploader.dao.entity.BuildInfo
import org.jetbrains.exposed.sql.insert

class BuildInfoRepositoryImpl : BuildInfoRepository {
    override fun insert(buildInfoDto: BuildInfoDto) {
        BuildInfo.insert {
            it[buildNumber] = buildInfoDto.buildId
            it[product_info] = buildInfoDto.info
        }
    }
}