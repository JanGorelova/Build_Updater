package com.uploader.dao.repository

import com.uploader.dao.dto.BuildInfoDto

interface BuildInfoRepository {
    fun insert(buildInfoDto: BuildInfoDto)
}