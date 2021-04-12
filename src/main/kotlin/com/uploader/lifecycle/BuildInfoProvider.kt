package com.uploader.lifecycle

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.uploader.lifecycle.Constants.RIDER
import com.uploader.lifecycle.Constants.UPDATES_URL
import com.uploader.lifecycle.xml.BuildData
import com.uploader.lifecycle.xml.Products
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import mu.KLogging
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
                            channelId = channel.id,
                            fullNumber = build.fullNumber ?: "Not specified",
                            version = getVersion(build, product.name),
                            releaseDate = build.releaseDate
                        )
                    }
                }
            }.toList()

    // todo "Temporary for version fix for rider product with version 2020.1, should be 2020.1.0"
    private fun getVersion(buildData: BuildData, productName: String) =
        when {
            productName == RIDER && buildData.version == "2020.1" -> "2020.1.0"
            else -> buildData.version
        }

    data class BuildUpdateInformation(
        val productName: String,
        val channelId: String,
        val fullNumber: String,
        val version: String,
        val releaseDate: LocalDate?
    )

    private companion object : KLogging()
}
