package com.uploader.unit.schedule

import com.uploader.refresh.InformationRefresher
import com.uploader.schedule.RefreshProductsInformationTask
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.component.KoinApiExtension
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@KoinApiExtension
class RefreshProductsInformationTaskTest : KoinTest {
    private lateinit var refresher: InformationRefresher

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(
            module {
                single { refresher }
            }
        )
    }

    @BeforeEach
    fun setup() {
        refresher = mock()
    }

    @Test
    fun `should refresh product information`() {
        // when
        RefreshProductsInformationTask().run()

        // then
        verify(refresher).refresh()
    }
}
