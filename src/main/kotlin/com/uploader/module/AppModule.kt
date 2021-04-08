package com.uploader.module

import com.uploader.config.AppConfig
import com.uploader.module.resource.ProductInfoRefresherResourceModule
import com.uploader.module.resource.ProductInfoResourceModule
import com.uploader.module.resource.StatusResourceModule
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CallLogging
import org.koin.core.component.KoinApiExtension
import org.koin.ktor.ext.Koin

@KoinApiExtension
object AppModule {
    @Suppress("unused")
    fun Application.module(config: AppConfig) {
        install(CallLogging)
        install(Koin) {
            modules(KoinCommonModule.module(config), KoinJobsModule.module(config))
        }

        ProductInfoResourceModule.module(this)
        ProductInfoRefresherResourceModule.module(this)
        StatusResourceModule.module(this)
        KtorModule.module(this)
    }
}
