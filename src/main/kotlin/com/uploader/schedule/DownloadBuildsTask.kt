package com.uploader.schedule

import com.uploader.dao.dto.BuildDto.State.CREATED
import com.uploader.dao.dto.BuildDto.State.FAILED
import com.uploader.dao.repository.BuildRepository
import com.uploader.db.DatabaseProvider
import com.uploader.provider.BuildDownloader
import java.util.TimerTask
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class DownloadBuildsTask : TimerTask(), KoinComponent {
    private val buildRepository by inject<BuildRepository>()
    private val buildDownloader by inject<BuildDownloader>()
    private val provider by inject<DatabaseProvider>()

    override fun run() {
        runBlocking { provider.dbQuery { buildRepository.gelAllWithStates(listOf(CREATED, FAILED)) } }
//            .filter {
//                val v = it.version
//                v in listOf("2020.1.4", "2020.3.3 RC2")
//            }
            .forEach { buildDto ->
                GlobalScope.launch { buildDownloader.download(buildDto) }
            }
    }
}
