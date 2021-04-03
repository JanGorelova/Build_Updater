package com.uploader

import com.uploader.container.TestDatabase
import com.uploader.db.DatabaseProvider
import io.ktor.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KtorExperimentalAPI
class StartSpec : KoinComponent {
    private val provider by inject<DatabaseProvider>()

    @Test
    fun test() {
        TestDatabase().use {
            TestApp().use {
                runBlocking {
                    delay(200000)
                }
            }
        }
    }
}