package com.uploader.provider

import com.uploader.dao.dto.BuildDto
import com.uploader.dao.dto.BuildDto.State.CREATED
import com.uploader.dao.dto.BuildDto.State.PROCESSING
import com.uploader.dao.repository.BuildRepository
import com.uploader.db.DatabaseProvider
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.apache.commons.io.FileUtils
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.nio.file.Paths

@ObsoleteCoroutinesApi
@KoinApiExtension
class BuildDownloader : KoinComponent {
    private val linkProvider by inject<DownloadLinkProvider>()
    private val client by inject<HttpClient>()
    private val provider by inject<DatabaseProvider>()
    private val buildRepository by inject<BuildRepository>()

    suspend fun download(buildDto: BuildDto) {
        val buildId = buildDto.id ?: error("Build id must be specified for $buildDto")
        provider.dbQuery { buildRepository.processing(buildId, CREATED) }

        val file = createFile(buildDto)
        val previousState = PROCESSING
        try {
            download(buildDto, file)
            provider.dbQuery { buildRepository.downloaded(buildId, previousState, file.path) }
        } catch (e: Exception) {
            provider.dbQuery { buildRepository.failed(buildId, previousState) }
            throw e
        }
    }

    private fun createFile(buildDto: BuildDto): File {
        val path = Paths.get("").toRealPath()
        val directory = "$path/src/main/resources/apps/${buildDto.productCode}"
        File(directory).mkdirs()

        return File("$directory/${buildDto.fullNumber}.tar.gz").also {
            it.createNewFile()
        }
    }

    private suspend fun download(buildDto: BuildDto, file: File) {
        val link = linkProvider[buildDto]

        var contentLength: Long? = null
        client.get<HttpStatement>(link).execute { r ->
            contentLength = r.headers["content-length"]?.toLong()

            r.receive<ByteReadChannel>().copyTo(file.outputStream())
        }

        val length = contentLength ?: error("Length for $link file can't be specified")
        verifyIntegrity(file, length)
    }

    private fun verifyIntegrity(file: File, expectedContentLength: Long) {
        val size = FileUtils.sizeOf(file)

        if (size != expectedContentLength)
            throw RuntimeException("Downloaded file size: $size differs from expected: $expectedContentLength")
    }
}