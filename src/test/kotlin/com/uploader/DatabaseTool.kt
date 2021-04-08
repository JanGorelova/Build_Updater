package com.uploader

import com.uploader.dao.entity.Build
import com.uploader.dao.entity.BuildInfo
import com.uploader.db.DatabaseProvider
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.selectAll
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
object DatabaseTool : KoinComponent {
    private val databaseProvider by inject<DatabaseProvider>()

    fun getAllBuilds() = runBlocking { databaseProvider.dbQuery { Build.selectAll().toList() } }
    fun getAllBuildInfos() = runBlocking { databaseProvider.dbQuery { BuildInfo.selectAll().toList() } }
}
