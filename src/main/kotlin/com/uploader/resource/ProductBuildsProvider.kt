package com.uploader.resource

import com.fasterxml.jackson.databind.JsonNode
import com.uploader.dao.repository.BuildInfoRepository
import com.uploader.db.DatabaseProvider
import com.uploader.lifecycle.Constants.getProductNameByProductCode
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
                val productName = getProductNameByProductCode(productCode)
                buildInfoRepository.findAllByProductName(productName)
            }
        }

    fun provideByProductCodeAndBuild(productCode: String, buildNumber: String): JsonNode =
        runBlocking {
            val productName = getProductNameByProductCode(productCode)

            provider.dbQuery {
                buildInfoRepository.findByProductNameAndBuildNumber(productName, buildNumber)
            }
        }
}
