package com.uploader

import com.uploader.TestingConstants.PYCHARM_2_CHANNEL
import com.uploader.TestingConstants.PYCHARM_2_FULL_NUMBER
import com.uploader.TestingConstants.PYCHARM_2_VERSION
import com.uploader.TestingConstants.PYCHARM_CHANNEL
import com.uploader.TestingConstants.PYCHARM_FULL_NUMBER
import com.uploader.TestingConstants.PYCHARM_VERSION
import com.uploader.TestingConstants.WEBSTORM_CHANNEL
import com.uploader.TestingConstants.WEBSTORM_FULL_NUMBER
import com.uploader.TestingConstants.WEBSTORM_VERSION
import com.uploader.dao.dto.BuildDto
import com.uploader.dao.dto.BuildDto.State.CREATED
import com.uploader.dao.dto.BuildInfoDto
import com.uploader.lifecycle.Constants.PYCHARM
import com.uploader.lifecycle.Constants.WEBSTORM

object TestingData {
    val py1BuildDto = BuildDto(
        productName = PYCHARM,
        fullNumber = PYCHARM_FULL_NUMBER,
        channelId = PYCHARM_CHANNEL,
        version = PYCHARM_VERSION,
        state = CREATED
    )

    val py2BuildDto = BuildDto(
        productName = PYCHARM,
        fullNumber = PYCHARM_2_FULL_NUMBER,
        channelId = PYCHARM_2_CHANNEL,
        version = PYCHARM_2_VERSION,
        state = CREATED
    )

    val wsBuildDto = BuildDto(
        productName = WEBSTORM,
        fullNumber = WEBSTORM_FULL_NUMBER,
        channelId = WEBSTORM_CHANNEL,
        version = WEBSTORM_VERSION,
        state = CREATED
    )

    fun buildInfoDto(buildId: Int, info: String) =
        BuildInfoDto(
            buildId = buildId,
            info = info
        )
}
