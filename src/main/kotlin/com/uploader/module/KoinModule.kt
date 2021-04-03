package com.uploader.module

import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.uploader.config.AppConfig
import com.uploader.dao.repository.BuildInfoRepository
import com.uploader.dao.repository.BuildInfoRepositoryImpl
import com.uploader.dao.repository.BuildRepository
import com.uploader.dao.repository.BuildRepositoryImpl
import com.uploader.db.DatabaseProvider
import com.uploader.provider.*
import io.ktor.client.*
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.apache.commons.vfs2.VFS
import org.koin.core.component.KoinApiExtension
import org.koin.dsl.module

@ObsoleteCoroutinesApi
@KoinApiExtension
class KoinModule {
    fun module(configuration: AppConfig) =
        module {
            single { configuration }
            single { DatabaseProvider() }
            single { HttpClient() }
            single<BuildRepository> { BuildRepositoryImpl() }
            single<BuildInfoProvider> { BuildInfoProvider() }
            single { BuildUpdatesPersister() }
            single { BuildDownloader() }
            single<BuildInfoRepository> { BuildInfoRepositoryImpl() }
            single { DownloadLinkProvider() }
            single { ProductInfoRetriever() }
            single { VFS.getManager() }
            factory { ProductInfoTarPathProvider() }
            single { mapper() }
        }

    private fun mapper(): XmlMapper {
        val mapper = XmlMapper()
        mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
        mapper.registerModule(KotlinModule())
        mapper.findAndRegisterModules()

        return mapper
    }
}