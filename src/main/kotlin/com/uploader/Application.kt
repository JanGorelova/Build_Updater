package com.uploader

import org.koin.core.component.KoinApiExtension

@KoinApiExtension
fun main(args: Array<String>) {
    val config = ConfigLoader().extractConfig()
    App(config).start()
}
