package com.uploader.dao.repository

import com.uploader.dao.dto.BuildDto

interface BuildRepository {
    suspend fun insert(buildDto: BuildDto): Int

    suspend fun getByFullNumberAndChannel(fullNumber: String, channelId: String): BuildDto?

    suspend fun processing(id: Int, previousState: BuildDto.State)

    suspend fun failed(id: Int, previousState: BuildDto.State)

    suspend fun downloaded(id: Int, previousState: BuildDto.State, path: String)

    suspend fun gelAllWithStates(states: List<BuildDto.State>): List<BuildDto>
}
