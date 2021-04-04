package com.uploader.resource

import com.fasterxml.jackson.databind.JsonNode
import com.uploader.dao.repository.BuildInfoRepository
import com.uploader.db.DatabaseProvider
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class ProductBuildsProvider : KoinComponent {
    private val buildInfoRepository by inject<BuildInfoRepository>()
    private val provider by inject<DatabaseProvider>()

    fun provideByProduct(productCode: String): Map<String, JsonNode> =
        runBlocking {
            provider.dbQuery {
                buildInfoRepository.findAllByProductCode(productCode)
            }
        }

    fun provideByProductAndBuild(productCode: String, buildNumber: String): JsonNode =
        runBlocking {
            provider.dbQuery {
                buildInfoRepository.findByProductCodeAndBuildNumber(productCode, buildNumber)
            }
        }
}
