package com.uploader.resource

import com.fasterxml.jackson.databind.JsonNode
import com.uploader.dao.repository.BuildInfoRepository
import com.uploader.lifecycle.Constants.getProductNameByProductCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class ProductBuildsProvider : KoinComponent {
    private val buildInfoRepository by inject<BuildInfoRepository>()

    fun provideByProduct(productCode: String): Map<String, JsonNode> =
        runBlocking {
            val productName = getProductNameByProductCode(productCode)
            buildInfoRepository.findAllByProductName(productName)
        }

    suspend fun provideByProductCodeAndBuild(productCode: String, buildNumber: String): JsonNode =
        withContext(Dispatchers.Default) {
            val productName = getProductNameByProductCode(productCode)

            buildInfoRepository.findByProductNameAndBuildNumber(productName, buildNumber)
        }
}
