package com.uploader.provider

import com.uploader.dao.dto.BuildInfoDto
import com.uploader.dao.repository.BuildInfoRepository
import com.uploader.db.DatabaseProvider
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@ObsoleteCoroutinesApi
@KoinApiExtension
class BuildInfoPersister : KoinComponent {
    private val productInfoRetriever by inject<ProductInfoRetriever>()
    private val buildInfoRepository by inject<BuildInfoRepository>()
    private val provider by inject<DatabaseProvider>()

    suspend fun persistInfo(path: String, buildId: Int) {
        val info = productInfoRetriever.retrieve(path)

        val buildInfo = BuildInfoDto(buildId = buildId, info = info)
        provider.dbQuery { buildInfoRepository.insert(buildInfo) }
    }
}