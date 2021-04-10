package com.uploader.spec

import com.uploader.TestApp
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent

@KoinApiExtension
class StartSpec : KoinComponent {
//    @Test
    fun test() {
    TestApp()
    runBlocking {
        delay(20000000)
    }
}
}
