package com.uploader.unit.lifecycle

import com.uploader.TestingConstants.PYCHARM_EXPECTED_INFO_JSON
import com.uploader.TestingTool.downloadFromResource
import com.uploader.TestingTool.getResourceFullPath
import com.uploader.lifecycle.ProductInfoProvider
import net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.koin.core.component.KoinApiExtension
import org.koin.test.KoinTest

@KoinApiExtension
class ProductInfoProviderTest : KoinTest {
    @Test
    fun `should provide product info`() {
        // given
        val path = getResourceFullPath("app/tars/pycharm.tar.gz")

        // when
        val productInfo = ProductInfoProvider().find(path)

        // then
        assertJsonEquals(downloadFromResource(PYCHARM_EXPECTED_INFO_JSON).decodeToString(), productInfo)
    }

    @Test
    fun `should fail if product info was not found in tar file`() {
        // given
        val path = getResourceFullPath("app/tars/illegal/pycharm-without-info-file.tar.gz")

        // when
        val invocation: () -> Unit = { ProductInfoProvider().find(path) }

        // then
        assertThrows<IllegalStateException>(invocation)
    }
}
