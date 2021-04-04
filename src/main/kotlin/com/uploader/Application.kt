package com.uploader

import org.koin.core.component.KoinApiExtension

@KoinApiExtension
fun main(args: Array<String>) {
    print(args)
    App("dev").start()
}
