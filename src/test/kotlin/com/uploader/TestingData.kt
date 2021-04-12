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
import com.uploader.TestingTool.downloadFromResource
import com.uploader.dao.dto.BuildDto
import com.uploader.dao.dto.BuildDto.State.CREATED
import com.uploader.dao.dto.BuildInfoDto
import com.uploader.lifecycle.BuildInfoProvider.BuildUpdateInformation
import com.uploader.lifecycle.Constants.PYCHARM
import com.uploader.lifecycle.Constants.WEBSTORM
import java.time.LocalDate
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
object TestingData {
    fun productsUpdates() = downloadFromResource("updates/test_with_two_products.xml")

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

    val ws1BuildUpdate = BuildUpdateInformation(
        productName = WEBSTORM,
        channelId = WEBSTORM_CHANNEL,
        fullNumber = WEBSTORM_FULL_NUMBER,
        version = WEBSTORM_VERSION,
        releaseDate = LocalDate.of(2021, 4, 6)
    )

    val ws2BuildUpdate = BuildUpdateInformation(
        productName = WEBSTORM,
        channelId = WEBSTORM_CHANNEL,
        fullNumber = "203.7717.59",
        version = "2019.3.3",
        releaseDate = LocalDate.of(2019, 11, 30)
    )

    val py1BuildUpdate = buildUpdateInformation(
        productName = PYCHARM,
        channelId = PYCHARM_CHANNEL,
        fullNumber = PYCHARM_FULL_NUMBER,
        version = PYCHARM_VERSION,
        releaseDate = LocalDate.of(2021, 4, 7)
    )

    val py2BuildUpdate = buildUpdateInformation(
        productName = PYCHARM,
        channelId = PYCHARM_2_CHANNEL,
        fullNumber = PYCHARM_2_FULL_NUMBER,
        version = PYCHARM_2_VERSION
    )

    val py3BuildUpdate = buildUpdateInformation(
        productName = PYCHARM,
        channelId = PYCHARM_CHANNEL,
        fullNumber = "203.7717.81",
        version = "2019.3.5",
        releaseDate = LocalDate.of(2019, 12, 2)
    )

    fun buildInfoDto(buildId: Int = 1, info: String = """{"test": "info"}""") =
        BuildInfoDto(
            buildId = buildId,
            info = info
        )

    fun buildUpdateInformation(
        productName: String = PYCHARM,
        channelId: String = PYCHARM_CHANNEL,
        fullNumber: String = PYCHARM_FULL_NUMBER,
        version: String = PYCHARM_VERSION,
        releaseDate: LocalDate? = null
    ) =
        BuildUpdateInformation(
            productName = productName,
            channelId = channelId,
            fullNumber = fullNumber,
            version = version,
            releaseDate = releaseDate
        )

    fun buildUpdatesInfo(): List<BuildUpdateInformation> {
        return listOf(ws1BuildUpdate, ws2BuildUpdate, py1BuildUpdate, py3BuildUpdate, py2BuildUpdate)
    }
}
