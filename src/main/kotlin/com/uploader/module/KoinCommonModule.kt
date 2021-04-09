package com.uploader.module

import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.uploader.config.AppConfig
import com.uploader.dao.repository.BuildInfoRepository
import com.uploader.dao.repository.BuildInfoRepositoryImpl
import com.uploader.dao.repository.BuildRepository
import com.uploader.dao.repository.BuildRepositoryImpl
import com.uploader.db.DatabaseProvider
import com.uploader.module.HicariProvider.hikari
import com.uploader.provider.BuildDownloader
import com.uploader.provider.BuildInfoPersister
import com.uploader.provider.BuildInfoProvider
import com.uploader.provider.BuildUpdatesPersister
import com.uploader.provider.ChecksumVerifier
import com.uploader.provider.DownloadInfoGenerator
import com.uploader.provider.ProductInfoProvider
import com.uploader.refresh.InformationRefresher
import com.uploader.resource.BuildsStatusInfoProvider
import com.uploader.resource.ProductBuildsProvider
import io.ktor.client.HttpClient
import io.ktor.client.features.HttpTimeout
import org.joda.time.format.DateTimeFormat
import org.koin.core.component.KoinApiExtension
import org.koin.dsl.module

@KoinApiExtension
object KoinCommonModule {
    fun module(configuration: AppConfig) =
        module {
            single { configuration }
            single { DatabaseProvider() }
            single<HttpClient> {
                HttpClient {
                    install(HttpTimeout) {
                        requestTimeoutMillis = 5000
                    }
                }
            }
            single<BuildRepository> { BuildRepositoryImpl() }
            single { BuildInfoProvider() }
            single { BuildUpdatesPersister() }
            single { BuildDownloader() }
            single<BuildInfoRepository> { BuildInfoRepositoryImpl() }
            single { DownloadInfoGenerator() }
            factory { ProductInfoProvider() }
            single { mapper() }
            single { BuildInfoPersister() }
            single { BuildsStatusInfoProvider() }
            single { jacksonObjectMapper() }
            single { ProductBuildsProvider() }
            single { InformationRefresher() }
            single { ChecksumVerifier() }
            single { DateTimeFormat.forPattern("MM/dd/yyyy HH:mm") }
            single { hikari(configuration) }
        }

    private fun mapper(): XmlMapper {
        val mapper = XmlMapper()
        mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
        mapper.registerModule(KotlinModule())
        mapper.findAndRegisterModules()

        return mapper
    }
}
