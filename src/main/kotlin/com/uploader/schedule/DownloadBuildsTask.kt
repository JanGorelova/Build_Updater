package com.uploader.schedule

import com.uploader.dao.dto.BuildDto.State.CREATED
import com.uploader.dao.dto.BuildDto.State.FAILED
import com.uploader.dao.repository.BuildRepository
import com.uploader.lifecycle.BuildDownloader
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

    override fun run() {
        runBlocking { buildRepository.gelAllWithStates(listOf(CREATED, FAILED)) }
            .forEach { buildDto ->
                GlobalScope.launch { buildDownloader.download(buildDto) }
            }
    }
}
