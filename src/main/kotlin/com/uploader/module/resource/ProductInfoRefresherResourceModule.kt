package com.uploader.module.resource

import com.uploader.module.resource.RoutingHelper.PRODUCT_CODE
import com.uploader.module.resource.RoutingHelper.extractParamValue
import com.uploader.module.resource.RoutingHelper.ok
import com.uploader.refresh.InformationRefresher
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.routing.patch
import io.ktor.routing.routing
import mu.KLogging
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
object ProductInfoRefresherResourceModule : KoinComponent, KLogging() {
    private val informationRefresher by inject<InformationRefresher>()

    @Suppress("unused")
    fun module(application: Application) {
        with(application) {
            routing {
                patch("/refresh") {
                    logger.info { "Call refresh" }
                    informationRefresher.refresh()
                    call.ok()
                }
                patch("/refresh/{$PRODUCT_CODE}") {
                    val code = call.extractParamValue(PRODUCT_CODE)

                    logger.info { "Call refresh for $code" }
                    informationRefresher.refresh(setOf(code))

                    call.ok()
                }
            }
        }
    }
}
