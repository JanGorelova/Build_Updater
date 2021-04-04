package com.uploader.module

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.response.respond
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
object KtorModule {
    @Suppress("unused")
    fun module(application: Application) {
        with(application) {
            install(StatusPages) {
                exception<Throwable> {
                    call.respond(InternalServerError)
                }
            }
        }
    }
}
