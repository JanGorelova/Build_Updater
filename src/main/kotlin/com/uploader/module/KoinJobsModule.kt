package com.uploader.module

import com.uploader.config.AppConfig
import com.uploader.module.JobType.BUILD_DOWNLOAD
import com.uploader.module.JobType.PERSIST_PRODUCT_INFO
import com.uploader.module.JobType.REFRESH_PRODUCT_INFORMATION
import com.uploader.schedule.DownloadBuildsTask
import com.uploader.schedule.Job
import com.uploader.schedule.PersistProductInfosTask
import com.uploader.schedule.RefreshProductsInformationTask
import org.koin.core.component.KoinApiExtension
import org.koin.core.qualifier.named
import org.koin.dsl.module

@KoinApiExtension
class KoinJobsModule {
    fun module(configuration: AppConfig) =
        module {
            val refreshJobConfig = configuration.jobs[REFRESH_PRODUCT_INFORMATION]
            if (refreshJobConfig?.enabled == true) {
                single(named(REFRESH_PRODUCT_INFORMATION), createdAtStart = true) {
                    Job(
                        task = RefreshProductsInformationTask(),
                        name = "Build info update",
                        delay = refreshJobConfig.delay,
                        period = refreshJobConfig.period
                    )
                }
            }

            val buildDownloadJobConfig = configuration.jobs[BUILD_DOWNLOAD]
            if (buildDownloadJobConfig?.enabled == true) {
                single(named("Build"), createdAtStart = true) {
                    Job(
                        task = DownloadBuildsTask(),
                        name = "Build download",
                        delay = buildDownloadJobConfig.delay,
                        period = buildDownloadJobConfig.period
                    )
                }
            }

            val persistProductInfoJobConfig = configuration.jobs[PERSIST_PRODUCT_INFO]
            if (persistProductInfoJobConfig?.enabled == true) {
                single(named("Persist"), createdAtStart = true) {
                    Job(
                        task = PersistProductInfosTask(),
                        name = "Build info persist",
                        delay = persistProductInfoJobConfig.delay,
                        period = persistProductInfoJobConfig.period
                    )
                }
            }
        }
}
