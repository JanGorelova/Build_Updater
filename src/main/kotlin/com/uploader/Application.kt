package com.uploader

import org.koin.core.component.KoinApiExtension

@KoinApiExtension
fun main() {
    App(ConfigLoader().extractConfig()).start()
}
