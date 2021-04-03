package com.uploader.module

import com.uploader.config.AppConfig
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.koin.core.component.KoinApiExtension
import org.koin.ktor.ext.Koin

@KoinApiExtension
@ObsoleteCoroutinesApi
object AppModule {
    @KtorExperimentalAPI
    @Suppress("unused")
    fun Application.module(config: AppConfig) {
        install(CallLogging)
        routing {
            get("/") {
                call.respondText("Hello, world!")
            }
        }

        install(Koin) {
            modules(KoinModule().module(config))
        }
    }
}