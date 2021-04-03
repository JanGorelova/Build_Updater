package com.uploader.schedule

import com.uploader.dao.dto.BuildDto.State.CREATED
import com.uploader.dao.repository.BuildRepository
import com.uploader.db.DatabaseProvider
import com.uploader.provider.BuildDownloader
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

@ObsoleteCoroutinesApi
@KoinApiExtension
class DownloadBuildsTask : TimerTask(), KoinComponent {
    private val buildRepository by inject<BuildRepository>()
    private val buildDownloader by inject<BuildDownloader>()
    private val provider by inject<DatabaseProvider>()

    override fun run() {
        GlobalScope.launch {
            provider.dbQuery {
                buildRepository.gelAllWithStates(listOf(CREATED))
            }
                .filter { it.fullNumber == "211.6693.66" }
                .forEach { buildDto ->
                    GlobalScope.launch { buildDownloader.download(buildDto) }
                }
        }
    }
}