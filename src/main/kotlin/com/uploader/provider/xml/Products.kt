package com.uploader.provider.xml

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import java.time.LocalDate

@JacksonXmlRootElement(localName = "products")
data class Products(
    @field:JacksonXmlProperty(localName = "product")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    val products: List<Product>
)

data class Product(
    @field:JacksonXmlProperty(isAttribute = true)
    val name: String,
    @field:JacksonXmlProperty(localName = "code")
    val code: String,
    @field:JacksonXmlProperty(localName = "channel")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    val channels: List<Channel>
)

data class Channel(
    @field:JacksonXmlProperty(isAttribute = true)
    val id: String,
    @field:JacksonXmlProperty(isAttribute = true)
    val name: String,
    @field:JacksonXmlProperty(localName = "build")
    @field:JacksonXmlElementWrapper(useWrapping = false)
    val builds: List<Build>
)

data class Build(
    @field:JacksonXmlProperty(isAttribute = true)
    val number: String,
    @field:JacksonXmlProperty(isAttribute = true)
    val version: String,
    @field:JacksonXmlProperty(isAttribute = true)
    val fullNumber: String?,
    @field:JacksonXmlProperty(isAttribute = true)
    @field:JsonFormat(pattern = "yyyyMMdd", shape = STRING)
    val releaseDate: LocalDate?
)
