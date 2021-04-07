package com.uploader.provider

import com.uploader.dao.dto.BuildDto
import com.uploader.dao.dto.BuildDto.State.CREATED
import com.uploader.dao.repository.BuildRepository
import com.uploader.db.DatabaseProvider
import com.uploader.provider.BuildInfoProvider.BuildUpdateInformation
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class BuildUpdatesPersister : KoinComponent {
    private val buildRepository by inject<BuildRepository>()
    private val provider by inject<DatabaseProvider>()

    suspend fun saveBuildUpdateIfRequired(buildUpdateInformation: BuildUpdateInformation) {
        provider.dbQuery {
            val fullNumber = buildUpdateInformation.fullNumer
            val channelName = buildUpdateInformation.channelId

            if (buildRepository.getBy(fullNumber, channelName) != null) return@dbQuery

            val buildDto = BuildDto(
                fullNumber = buildUpdateInformation.fullNumer,
                channelId = buildUpdateInformation.channelId,
                state = CREATED,
                version = buildUpdateInformation.version,
                productName = buildUpdateInformation.productName
            )

            buildRepository.insert(buildDto)
        }
    }
}
