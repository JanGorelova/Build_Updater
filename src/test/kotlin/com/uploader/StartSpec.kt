package com.uploader

import com.uploader.db.DatabaseProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class StartSpec : KoinComponent {
    private val provider by inject<DatabaseProvider>()

//    @Test
    fun test() {
    TestApp()
    runBlocking {
        delay(20000000)
    }
}
}
