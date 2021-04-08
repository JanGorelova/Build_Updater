package com.uploader.module

import com.uploader.config.AppConfig
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
            modules(KoinCommonModule().module(config), KoinJobsModule().module(config))
        }

        ProductInfoModule.module(this)
        ProductInfoRefresherModule.module(this)
        StatusRoutingModule.module(this)
        KtorModule.module(this)
    }
}
