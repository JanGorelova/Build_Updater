package com.uploader.schedule

import com.uploader.dao.dto.BuildDto.State.DOWNLOADED
import com.uploader.dao.repository.BuildRepository
import com.uploader.lifecycle.BuildInfoPersister
import java.util.TimerTask
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class PersistProductInfosTask : TimerTask(), KoinComponent {
    private val buildRepository by inject<BuildRepository>()
    private val buildInfoPersister by inject<BuildInfoPersister>()

    override fun run() {
        runBlocking { buildRepository.gelAllWithStates(listOf(DOWNLOADED)) }
            .forEach { buildDto ->
                GlobalScope.launch {
                    val id = buildDto.id ?: error("Id must be specified for $buildDto")
                    val path = buildDto.path ?: error("Path must be specified for ${buildDto.id}")

                    buildInfoPersister.persistIfRequired(path, id)
                }
            }
    }

    private companion object : KLogging()
}
