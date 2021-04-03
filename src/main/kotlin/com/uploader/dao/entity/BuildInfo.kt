package com.uploader.dao.entity

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.jodatime.CurrentDateTime
import org.jetbrains.exposed.sql.jodatime.datetime

object BuildInfo : IntIdTable() {
    val buildNumber = integer("build_id").references(Build.id)
    val product_info = varchar("info", 500)
    val dateCreated = datetime("date_created").defaultExpression(CurrentDateTime())
}