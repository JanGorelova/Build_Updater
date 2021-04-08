package com.uploader.module.resource

import com.uploader.module.resource.RoutingHelper.okJson
import com.uploader.resource.BuildsStatusInfoProvider
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.routing.get
import io.ktor.routing.routing
import kotlinx.html.TBODY
import kotlinx.html.THEAD
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
object StatusResourceModule : KoinComponent {
    private val buildsStatusInfoProvider by inject<BuildsStatusInfoProvider>()

    @Suppress("unused")
    fun module(application: Application) {
        with(application) {
            routing {
                get("/status") {
                    val buildInfos = buildsStatusInfoProvider.provide()
                    call.okJson(buildInfos)
                }
                get("/") {
                    call.respondHtml {
                        body {
                            h1 { +"Activity information" }
                            table {
                                thead { createHead() }
                                tbody { buildsStatusInfoProvider.provide().forEach { createRow(it) } }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun THEAD.createHead() {
        tr {
            th { +"Product name" }
            th { +"Full number" }
            th { +"Current state" }
            th { +"Created date" }
            th { +"Updated date" }
        }
    }

    private fun TBODY.createRow(it: BuildsStatusInfoProvider.BuildStatusInfo) {
        tr {
            td { +it.productName }
            td { +it.fullNumber }
            td { +it.state }
            td { +it.dateCreated }
            td { +it.dateUpdated }
        }
    }
}
