package com.uploader.lifecycle

import java.io.File
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.vfs2.impl.StandardFileSystemManager
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent

@KoinApiExtension
class ProductInfoProvider : KoinComponent {
    fun find(path: String): String {
        val filePath = findPathInTar(path) ?: error("Product info path was not found in $path")

        val standardManager = StandardFileSystemManager().also { manager -> manager.init() }
        val productJsonData = standardManager.use { manager ->
            manager.resolveFile("tgz:file:$path!/$filePath").content.byteArray
        }

        return productJsonData?.decodeToString() ?: error("Product info data must be specified for $path")
    }

    private fun findPathInTar(path: String): String? {
        val file = File(path)
        TarArchiveInputStream(GzipCompressorInputStream(file.inputStream())).use {
            var entry = it.nextEntry

            while (entry != null && entry.name.substringAfterLast("/") != PRODUCT_INFO_FILE) {
                entry = it.nextEntry
            }

            return entry?.name
        }
    }

    private companion object {
        const val PRODUCT_INFO_FILE = "product-info.json"
    }
}
