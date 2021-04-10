package com.uploader.module

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.uploader.config.AppConfig
import com.uploader.dao.repository.BuildInfoRepository
import com.uploader.dao.repository.BuildInfoRepositoryImpl
import com.uploader.dao.repository.BuildRepository
import com.uploader.dao.repository.BuildRepositoryImpl
import com.uploader.db.DatabaseProvider
import com.uploader.lifecycle.BuildDownloader
import com.uploader.lifecycle.BuildInfoPersister
import com.uploader.lifecycle.BuildInfoProvider
import com.uploader.lifecycle.BuildUpdatesPersister
import com.uploader.lifecycle.ChecksumVerifier
import com.uploader.lifecycle.DownloadInfoGenerator
import com.uploader.lifecycle.ProductInfoProvider
import com.uploader.module.HicariProvider.hikari
import com.uploader.module.XmlMapperProvider.xmlMapper
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
            single(createdAtStart = true) { DatabaseProvider() }
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
            single { xmlMapper() }
            single { BuildInfoPersister() }
            single { BuildsStatusInfoProvider() }
            single { jacksonObjectMapper() }
            single { ProductBuildsProvider() }
            single { InformationRefresher() }
            single { ChecksumVerifier() }
            single { DateTimeFormat.forPattern("MM/dd/yyyy HH:mm") }
            single { hikari(configuration) }
        }
}
