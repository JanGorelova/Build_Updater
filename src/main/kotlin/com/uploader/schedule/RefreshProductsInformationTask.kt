package com.uploader.schedule

import com.uploader.refresh.InformationRefresher
import java.util.TimerTask
import mu.KLogging
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class RefreshProductsInformationTask : TimerTask(), KoinComponent {
    private val refresher by inject<InformationRefresher>()

    override fun run() {
        refresher.refresh()
    }

    private companion object : KLogging()
}
