package com.uploader.schedule

import com.uploader.provider.BuildInfoProvider
import com.uploader.provider.BuildInfoProvider.BuildUpdateInformation
import com.uploader.provider.BuildUpdatesPersister
import com.uploader.provider.Constants.productNameToUrl
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDate
import java.util.*

@ObsoleteCoroutinesApi
@KoinApiExtension
class CheckNewBuildsTask : TimerTask(), KoinComponent {
    private val infoProvider by inject<BuildInfoProvider>()
    private val persister by inject<BuildUpdatesPersister>()

    override fun run() {
        infoProvider.provide()
            .filter { productNameToUrl.containsKey(it.productName) }
            .filter { it.isLessThanOneYear() }
            .forEach { GlobalScope.launch { persister.saveBuildUpdateIfRequired(it) } }
    }

    private fun BuildUpdateInformation.isLessThanOneYear(): Boolean {
        val currentYear = LocalDate.now().year
        val yearAgo = LocalDate.now().minusYears(1)

        return version.contains(currentYear.toString()) || version.contains(yearAgo.toString())
    }
}