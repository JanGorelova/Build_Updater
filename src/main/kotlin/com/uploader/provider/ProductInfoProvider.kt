package com.uploader.provider

import com.uploader.provider.Constants.productInfoFileName
import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.FileSystemManager
import org.apache.commons.vfs2.VFS
import org.apache.commons.vfs2.impl.StandardFileSystemManager
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent

@KoinApiExtension
class ProductInfoProvider : KoinComponent {
    private var originalFileObject: FileObject? = null

    fun find(path: String): String {
        return VFS.getManager().use {
            findRecursively("tgz:file:$path!/", it)

            val file = originalFileObject ?: error("")
            val standardManager = StandardFileSystemManager().also { manager -> manager.init() }
            val productJsonData = standardManager.use { manager ->
                manager.resolveFile(file.publicURIString).content.byteArray
            }

            productJsonData?.decodeToString() ?: error("Product info data must be specified for $path")
        }
    }

    private fun findRecursively(path: String, fileSystemManager: FileSystemManager): String? {
        val fileObject = fileSystemManager.resolveFile(path)

        return when {
            fileObject.isFile && fileObject.name.baseName == productInfoFileName -> {
                originalFileObject = fileObject
                fileObject.publicURIString
            }
            fileObject.isFile -> null
            else -> fileObject.children.firstOrNull {
                findRecursively(
                    it.publicURIString,
                    fileSystemManager
                ) != null
            }?.publicURIString
        }
    }
}
