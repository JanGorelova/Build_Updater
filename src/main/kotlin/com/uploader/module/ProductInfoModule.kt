package com.uploader.module

import com.uploader.module.RoutingHelper.BUILD_NUMBER
import com.uploader.module.RoutingHelper.PRODUCT_CODE
import com.uploader.module.RoutingHelper.extractParamValue
import com.uploader.module.RoutingHelper.okJson
import com.uploader.resource.ProductBuildsProvider
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.routing.get
import io.ktor.routing.routing
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
object ProductInfoModule : KoinComponent {
    private val productBuildsProvider by inject<ProductBuildsProvider>()

    @Suppress("unused")
    fun module(application: Application) {
        with(application) {
            routing {
                get("/{$PRODUCT_CODE}") {
                    val code = call.extractParamValue(PRODUCT_CODE)

                    val infos = productBuildsProvider.provideByProduct(code)

                    call.okJson(infos)
                }
                get("/{$PRODUCT_CODE}/{$BUILD_NUMBER}") {
                    val code = call.extractParamValue(PRODUCT_CODE)
                    val buildNumber = call.extractParamValue(BUILD_NUMBER)

                    val info = productBuildsProvider.provideByProductAndBuild(code, buildNumber)
                    call.okJson(info)
                }
            }
        }
    }
}
