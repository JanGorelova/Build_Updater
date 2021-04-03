package com.uploader.provider

import com.uploader.dao.dto.BuildDto
import com.uploader.provider.Constants.productNameToUrl
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent

@KoinApiExtension
class DownloadLinkProvider : KoinComponent {
    operator fun get(buildDto: BuildDto) : String {
        val productName = buildDto.productCode
        val productUrl = productNameToUrl[productName] ?: error("Unknown product name")

        return "$URL$productUrl-${buildDto.fullNumber}$suffix"
    }

    private companion object {
        private const val suffix = ".tar.gz"
        private const val URL = "https://download.jetbrains.com/"
    }
}