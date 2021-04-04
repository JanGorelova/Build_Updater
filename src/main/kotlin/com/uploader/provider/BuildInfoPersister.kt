package com.uploader.provider

import com.uploader.dao.dto.BuildInfoDto
import com.uploader.dao.repository.BuildInfoRepository
import com.uploader.db.DatabaseProvider
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class BuildInfoPersister : KoinComponent {
    private val productInfoProvider by inject<ProductInfoProvider>()
    private val buildInfoRepository by inject<BuildInfoRepository>()
    private val provider by inject<DatabaseProvider>()

    suspend fun persistIfRequired(path: String, buildId: Int) {
        val existing = provider.dbQuery { buildInfoRepository.findByBuildId(buildId) }
        if (existing != null) return

        val info = productInfoProvider.find(path)

        val buildInfo = BuildInfoDto(buildId = buildId, info = info)
        provider.dbQuery { buildInfoRepository.insert(buildInfo) }
    }
}
