package com.uploader.unit.refresh

import com.uploader.TestingData.buildUpdatesInfo
import com.uploader.TestingData.py1BuildUpdate
import com.uploader.TestingData.py2BuildUpdate
import com.uploader.TestingData.ws1BuildUpdate
import com.uploader.lifecycle.BuildInfoProvider
import com.uploader.lifecycle.BuildUpdatesPersister
import com.uploader.refresh.InformationRefresher
import kotlinx.coroutines.runBlocking
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.component.KoinApiExtension
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

@KoinApiExtension
class InformationRefresherTest : KoinTest {
    private lateinit var infoProvider: BuildInfoProvider
    private lateinit var persister: BuildUpdatesPersister

    private val updates = buildUpdatesInfo()

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(
            module {
                single { infoProvider }
                single { persister }
            }
        )
    }

    @BeforeEach
    fun setup() {
        infoProvider = mock()
        persister = mock()
    }

    @Test
    fun `should refresh released less than year ago`() {
        // given
        whenever(infoProvider.provide()).thenReturn(updates)

        // when
        runBlocking { InformationRefresher().refresh() }

        // then
        await().untilAsserted {
            runBlocking {
                verify(persister)
                    .saveBuildUpdateIfRequired(ws1BuildUpdate)
                verify(persister)
                    .saveBuildUpdateIfRequired(py1BuildUpdate)
                verify(persister)
                    .saveBuildUpdateIfRequired(py2BuildUpdate)
                verifyNoMoreInteractions(persister)
            }
        }
    }

    @Test
    fun `should refresh released less than year ago and with specified product`() {
        // given
        whenever(infoProvider.provide()).thenReturn(updates)

        // when
        runBlocking { InformationRefresher().refresh(productCodes = setOf("WS")) }

        // then
        await().untilAsserted {
            runBlocking {
                verify(persister)
                    .saveBuildUpdateIfRequired(ws1BuildUpdate)
                verifyNoMoreInteractions(persister)
            }
        }
    }
}
