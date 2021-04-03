package com.uploader.provider

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.uploader.provider.Constants.UPDATES_URL
import com.uploader.provider.xml.Products
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class BuildInfoProvider : KoinComponent {
    private val client by inject<HttpClient>()
    private val mapper by inject<XmlMapper>()

    fun provide(): List<BuildUpdateInformation> {
        val data = runBlocking {
            client.get<String>(UPDATES_URL)
        }

        return mapper.readValue(data, Products::class.java).toBuildInformation()
    }

    private fun Products.toBuildInformation(): List<BuildUpdateInformation> =
        products.asSequence()
            .flatMap { product ->
                product.channels.flatMap { channel ->
                    channel.builds.map { build ->
                        BuildUpdateInformation(
                            productName = product.name,
                            productCode = product.code,
                            channelId = channel.id,
                            fullNumer = build.fullNumber ?: "Not specified",
                            version = build.version
                        )
                    }
                }
            }.toList()

    data class BuildUpdateInformation(
        val productName: String,
        val productCode: String,
        val channelId: String,
        val fullNumer: String,
        val version: String
    )
}