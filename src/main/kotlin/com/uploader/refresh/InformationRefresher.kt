package com.uploader.refresh

import com.uploader.provider.BuildInfoProvider
import com.uploader.provider.BuildInfoProvider.BuildUpdateInformation
import com.uploader.provider.BuildUpdatesPersister
import com.uploader.provider.Constants.productNameToUrl
import java.time.LocalDate
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class InformationRefresher : KoinComponent {
    private val infoProvider by inject<BuildInfoProvider>()
    private val persister by inject<BuildUpdatesPersister>()

    fun refresh(productCodes: List<String>? = null) {
        GlobalScope.launch {
            infoProvider.provide()
                .filter { shouldBeRefreshed(productCodes, it.productName) }
                .filter { it.releasedLessThanYearAgo() }
                .forEach { GlobalScope.launch { persister.saveBuildUpdateIfRequired(it) } }
        }
    }

    private fun shouldBeRefreshed(productCodes: List<String>? = null, productName: String): Boolean {
        val productsToUpdate = productCodes?.let { codes ->
            productNameToUrl.keys.intersect(codes)
        } ?: productNameToUrl.keys

        return productName in productsToUpdate
    }

    private fun BuildUpdateInformation.releasedLessThanYearAgo(): Boolean {
        val currentYear = LocalDate.now().year
        val dateYearAgo = LocalDate.now().minusYears(1)

        this.releaseDate?.let { return it.isAfter(dateYearAgo) }

        return version.contains(currentYear.toString()) || version.contains(dateYearAgo.year.toString())
    }
}
