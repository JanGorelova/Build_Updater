package com.uploader.lifecycle

import com.uploader.config.AppConfig
import com.uploader.dao.dto.BuildDto
import java.io.File
import java.net.URI
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class FileHelper : KoinComponent {
    private val verifier by inject<ChecksumVerifier>()
    private val config by inject<AppConfig>()

    private val rootBuildsPath = config.rootBuildsPath

    fun directoryAndFilePath(buildDto: BuildDto): Pair<String, String> {
        val directory = URI("$rootBuildsPath${buildDto.productName.replace(" ", "_")}")
            .normalize()
            .path
        val filePath = URI("$directory/${buildDto.fullNumber}.tar.gz").normalize().path

        return directory to filePath
    }

    fun alreadyExists(filePath: String, checkSum: String): Boolean {
        val file = File(filePath)

        return when {
            !file.exists() -> false
            else -> verifier.isIntegral(file, checkSum).also {
                if (!it) file.delete()
            }
        }
    }

    fun createFile(directory: String, filePath: String): File {
        val file = File(filePath)
        if (file.exists())
            file.deleteRecursively()

        File(directory).mkdirs()

        return file.also { it.createNewFile() }
    }
}
