package com.uploader.provider

import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.apache.commons.vfs2.FileSystemManager
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@ObsoleteCoroutinesApi
@KoinApiExtension
class ProductInfoRetriever : KoinComponent {
    private val systemManager by inject<FileSystemManager>()
    private val productInfoTarPathProvider by inject<ProductInfoTarPathProvider>()

    suspend fun retrieve(path: String) : String {
        val productFilePath = productInfoTarPathProvider.find("tgz:file:$path!")
        val bytes = systemManager.resolveFile(productFilePath)
            .content
            .inputStream
            .readAllBytes()

        return bytes.decodeToString()
    }
}