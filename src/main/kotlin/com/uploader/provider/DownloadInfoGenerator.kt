package com.uploader.provider

import com.uploader.dao.dto.BuildDto
import com.uploader.provider.Constants.productNameToUrl
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class DownloadInfoGenerator : KoinComponent {
    private val client by inject<HttpClient>()

    operator fun get(buildDto: BuildDto): DownloadInfo {
        val productName = buildDto.productName
        val productUrl = productNameToUrl[productName] ?: error("Unknown product name")

        val buildNumber = when (buildDto.version.contains(" ")) {
            true -> buildDto.fullNumber
            else -> buildDto.version
        }

        val downloadLink = "$URL$productUrl-${buildNumber}$suffix"
        val checkSumLink = "$downloadLink.sha256"

        val expectedCheckSum = runBlocking { client.get<String>(checkSumLink) }

        print(expectedCheckSum)
        return DownloadInfo(
            downloadLink = downloadLink,
            checkSum = expectedCheckSum.substringBefore("*").trim()
        )
    }

    data class DownloadInfo(
        val downloadLink: String,
        val checkSum: String
    )

    private companion object {
        private const val suffix = ".tar.gz"
        private const val URL = "https://download.jetbrains.com/"
    }
}
