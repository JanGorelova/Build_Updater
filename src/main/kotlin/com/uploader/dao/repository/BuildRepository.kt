package com.uploader.dao.repository

import com.uploader.dao.dto.BuildDto

interface BuildRepository {
    fun insert(buildDto: BuildDto): Int

    fun getBy(fullNumber: String, channelId: String): BuildDto?

    fun processing(id: Int, previousState: BuildDto.State)

    fun failed(id: Int, previousState: BuildDto.State)

    fun downloaded(id: Int, previousState: BuildDto.State, path: String)

    fun gelAllWithStates(states: List<BuildDto.State>): List<BuildDto>
}
