package com.uploader.schedule

import com.uploader.dao.dto.BuildDto.State.DOWNLOADED
import com.uploader.dao.repository.BuildRepository
import com.uploader.db.DatabaseProvider
import com.uploader.provider.BuildInfoPersister
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

@ObsoleteCoroutinesApi
@KoinApiExtension
class PersistProductInfoTask : TimerTask(), KoinComponent {
    private val buildRepository by inject<BuildRepository>()
    private val provider by inject<DatabaseProvider>()
    private val buildInfoPersister by inject<BuildInfoPersister>()

    override fun run() {
        GlobalScope.launch {
            provider
                .dbQuery { buildRepository.gelAllWithStates(listOf(DOWNLOADED)) }
                .forEach { buildDto ->
                    GlobalScope.launch {
                        val id = buildDto.id ?: error("Id must be specified for $buildDto")
                        val path = buildDto.path ?: error("Path must be specified for ${buildDto.id}")
                        buildInfoPersister.persistInfo(path, id)
                    }
                }
        }
    }
}