package com.uploader.lifecycle

import com.uploader.dao.dto.BuildDto
import com.uploader.dao.dto.BuildDto.State.CREATED
import com.uploader.dao.repository.BuildRepository
import com.uploader.lifecycle.BuildInfoProvider.BuildUpdateInformation
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class BuildUpdatesPersister : KoinComponent {
    private val buildRepository by inject<BuildRepository>()

    suspend fun saveBuildUpdateIfRequired(buildUpdateInformation: BuildUpdateInformation) {
        val fullNumber = buildUpdateInformation.fullNumber
        val channelName = buildUpdateInformation.channelId

        if (buildRepository.getByFullNumberAndChannel(fullNumber, channelName) != null) return

        val buildDto = BuildDto(
            fullNumber = buildUpdateInformation.fullNumber,
            channelId = buildUpdateInformation.channelId,
            state = CREATED,
            version = buildUpdateInformation.version,
            productName = buildUpdateInformation.productName
        )

        buildRepository.insert(buildDto)
    }
}
