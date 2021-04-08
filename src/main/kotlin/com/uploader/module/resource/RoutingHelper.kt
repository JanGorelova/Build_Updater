package com.uploader.module.resource

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.content.TextContent
import io.ktor.response.respond
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
object RoutingHelper : KoinComponent {
    const val PRODUCT_CODE = "product-code"
    const val BUILD_NUMBER = "build-number"

    private val jsonMapper by inject<ObjectMapper>()

    suspend fun ApplicationCall.okJson(body: Any) {
        this.respond(OK, TextContent(jsonMapper.writeValueAsString(body), ContentType.Application.Json))
    }

    suspend fun ApplicationCall.ok() {
        this.respond(OK)
    }

    fun ApplicationCall.extractParamValue(name: String) =
        this.parameters[name] ?: error("Param $name must be specified")
}
