package com.uploader.module

import com.uploader.module.RoutingHelper.PRODUCT_CODE
import com.uploader.module.RoutingHelper.extractParamValue
import com.uploader.module.RoutingHelper.ok
import com.uploader.refresh.InformationRefresher
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.routing.patch
import io.ktor.routing.routing
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
object ProductInfoRefresherModule : KoinComponent {
    private val informationRefresher by inject<InformationRefresher>()

    @Suppress("unused")
    fun module(application: Application) {
        with(application) {
            routing {
                patch("/refresh") {
                    informationRefresher.refresh()
                    call.ok()
                }
                patch("/refresh/{$PRODUCT_CODE}") {
                    val code = call.extractParamValue(PRODUCT_CODE)

                    informationRefresher.refresh(setOf(code))

                    call.ok()
                }
            }
        }
    }
}
