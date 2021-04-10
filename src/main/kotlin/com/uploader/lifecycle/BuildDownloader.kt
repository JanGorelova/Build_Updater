package com.uploader.lifecycle

import com.uploader.config.AppConfig
import com.uploader.dao.dto.BuildDto
import com.uploader.dao.dto.BuildDto.State.PROCESSING
import com.uploader.dao.repository.BuildRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.features.timeout
import io.ktor.client.request.get
import io.ktor.client.statement.HttpStatement
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.copyTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KLogging
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class BuildDownloader : KoinComponent {
    private val downloadInfoGenerator by inject<DownloadInfoGenerator>()
    private val client by inject<HttpClient>()
    private val buildRepository by inject<BuildRepository>()
    private val verifier by inject<ChecksumVerifier>()
    private val fileHelper by inject<FileHelper>()
    private val config by inject<AppConfig>()

    suspend fun download(buildDto: BuildDto) {
        val buildId = buildDto.id ?: error("Build id must be specified for $buildDto")

        buildRepository.processing(buildId, buildDto.state)

        val previousState = PROCESSING
        try {
            val filePath = downloadIfRequiredAndReturnPath(buildDto)
            buildRepository.downloaded(buildId, previousState, filePath)
        } catch (e: Exception) {
            logger.error { "Couldn't update: $e" }
            buildRepository.failed(buildId, previousState)
        }
    }

    private suspend fun downloadIfRequiredAndReturnPath(buildDto: BuildDto): String {
        val downloadData = downloadInfoGenerator[buildDto]
        val (directory, filePath) = fileHelper.directoryAndFilePath(buildDto)

        if (fileHelper.alreadyExists(filePath, downloadData.checkSum)) return filePath

        val file = fileHelper.createFile(directory, filePath)
        withContext(Dispatchers.Default) {
            client.get<HttpStatement>(downloadData.downloadLink) {
                timeout {
                    requestTimeoutMillis = config.downloadRequestTimeout.toMillis()
                }
            }.execute { httpResponse -> file.outputStream().use { httpResponse.receive<ByteReadChannel>().copyTo(it) } }
        }

        if (!verifier.isIntegral(file, downloadData.checkSum))
            throw RuntimeException("Downloaded file is not integral")

        return filePath
    }

    private companion object : KLogging()
}
