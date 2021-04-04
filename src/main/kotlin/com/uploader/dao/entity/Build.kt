package com.uploader.dao.entity

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.jodatime.CurrentDateTime
import org.jetbrains.exposed.sql.jodatime.datetime

object Build : IntIdTable() {
    val fullNumber = varchar("full_number", 20).index()
    val channelId = varchar("channel_id", 50).index()
    val productName = varchar("product_name", 50).index()
    val productCode = varchar("product_code", 50).index()
    val version = varchar("version", 50).index()
    val state = varchar("state", 20)
    val path = varchar("path", 500).nullable()
    val dateCreated = datetime("date_created").defaultExpression(CurrentDateTime())
    val dateUpdated = datetime("date_updated").defaultExpression(CurrentDateTime())

    init {
        index(true, fullNumber, channelId)
    }
}
