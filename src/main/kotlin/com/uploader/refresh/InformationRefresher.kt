package com.uploader.refresh

import com.uploader.provider.BuildInfoProvider
import com.uploader.provider.BuildInfoProvider.BuildUpdateInformation
import com.uploader.provider.BuildUpdatesPersister
import com.uploader.provider.Constants.getProductNameByProductCode
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

    fun refresh(productCodes: Set<String>? = null) {
        val productNames = productCodes?.let { mapCodesToProductNames(it) }

        GlobalScope.launch {
            infoProvider.provide()
                .filter { shouldBeRefreshed(productNames, it.productName) }
                .filter { it.releasedLessThanYearAgo() }
                .forEach { GlobalScope.launch { persister.saveBuildUpdateIfRequired(it) } }
        }
    }

    private fun mapCodesToProductNames(productCodes: Set<String>) =
        productCodes.map {
            getProductNameByProductCode(it)
        }.toSet()

    private fun shouldBeRefreshed(productNames: Set<String>? = null, productName: String): Boolean {
        val productsToUpdate = productNames?.let { names ->
            productNameToUrl.keys.intersect(names)
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
