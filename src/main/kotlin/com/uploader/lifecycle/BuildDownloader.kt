package com.uploader.lifecycle

import com.uploader.config.AppConfig
import com.uploader.dao.dto.BuildDto
import com.uploader.dao.dto.BuildDto.State.PROCESSING
import com.uploader.dao.repository.BuildRepository
import com.uploader.db.DatabaseProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.features.timeout
import io.ktor.client.request.get
import io.ktor.client.statement.HttpStatement
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.copyTo
import java.io.File
import mu.KLogging
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class BuildDownloader : KoinComponent {
    private val downloadInfoGenerator by inject<DownloadInfoGenerator>()
    private val client by inject<HttpClient>()
    private val provider by inject<DatabaseProvider>()
    private val buildRepository by inject<BuildRepository>()
    private val verifier by inject<ChecksumVerifier>()
    private val config by inject<AppConfig>()

    private val path = config.rootBuildsPath

    suspend fun download(buildDto: BuildDto) {
        val buildId = buildDto.id ?: error("Build id must be specified for $buildDto")

        provider.dbQuery { buildRepository.processing(buildId, buildDto.state) }

        val previousState = PROCESSING
        try {
            val filePath = downloadIfRequiredAndReturnPath(buildDto)
            provider.dbQuery { buildRepository.downloaded(buildId, previousState, filePath) }
        } catch (e: Exception) {
            logger.error { "Couldn't update: $e" }
            provider.dbQuery { buildRepository.failed(buildId, previousState) }
        }
    }

    private suspend fun downloadIfRequiredAndReturnPath(buildDto: BuildDto): String {
        val downloadData = downloadInfoGenerator[buildDto]

        val directory = "$path${buildDto.productName.replace(" ", "_")}"
        val filePath = "$directory/${buildDto.fullNumber}.tar.gz"

        if (alreadyExists(filePath, downloadData.checkSum)) return filePath

        val file = createFile(directory, filePath)
        client.get<HttpStatement>(downloadData.downloadLink) { timeout { requestTimeoutMillis = 1200_000 } }
            .execute { httpResponse -> httpResponse.receive<ByteReadChannel>().copyTo(file.outputStream()) }

        if (!verifier.isIntegral(file, downloadData.checkSum))
            throw RuntimeException("Downloaded file is not integral")

        return filePath
    }

    private fun alreadyExists(filePath: String, checkSum: String): Boolean {
        val file = File(filePath)

        return when {
            !file.exists() -> false
            else -> verifier.isIntegral(file, checkSum).also {
                if (!it) file.delete()
            }
        }
    }

    private fun createFile(directory: String, filePath: String): File {
        val file = File(filePath)
        if (file.exists())
            file.deleteRecursively()

        File(directory).mkdirs()

        return file.also { it.createNewFile() }
    }

    private companion object : KLogging()
}
