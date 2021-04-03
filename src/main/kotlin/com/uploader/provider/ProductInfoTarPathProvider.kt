package com.uploader.provider

import com.uploader.provider.Constants.productInfoFileName
import org.apache.commons.vfs2.FileSystemManager
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class ProductInfoTarPathProvider : KoinComponent {
    private var originalPath: String? = null
    private val systemManager by inject<FileSystemManager>()

    fun find(path: String): String {
        findRecursively(path)

        return originalPath ?: error("Not found")
    }

    private fun findRecursively(path: String) : String? {
        val fileObject = systemManager.resolveFile(path)

        return when {
            fileObject.isFile && fileObject.name.baseName.contains(productInfoFileName) -> {
                originalPath = fileObject.publicURIString
                originalPath
            }
            fileObject.isFile -> null
            else -> fileObject.children.firstOrNull { findRecursively(it.publicURIString) != null}?.publicURIString
        }
    }
}