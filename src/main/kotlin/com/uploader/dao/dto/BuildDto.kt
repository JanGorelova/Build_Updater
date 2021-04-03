package com.uploader.dao.dto

import org.joda.time.LocalDateTime


data class BuildDto(
    val id: Int? = null,
    val productCode: String,
    val productName: String,
    val fullNumber:String,
    val channelId: String,
    val version: String,
    val state: State,
    val path: String? = null,
    val dateCreated: LocalDateTime? = null
) {
    enum class State {
        CREATED,
        PROCESSING,
        DOWNLOADED,
        FAILED
    }
}