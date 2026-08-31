package com.rohit.jobtracker.backend.db

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object ApplicationsTable : Table("applications") {
    val id = varchar("id", 64)
    val company = varchar("company", 255)
    val role = varchar("role", 255)
    val appSource = varchar("source", 50)
    val dateApplied = date("date_applied")
    val jobLink = varchar("job_link", 1024).nullable()
    val status = varchar("status", 50)
    val lastUpdated = timestamp("last_updated")
    val reminderDays = integer("reminder_days").nullable()
    val salary = varchar("salary", 100).nullable()

    override val primaryKey = PrimaryKey(id)
}

object NotesTable : Table("notes") {
    val id = varchar("id", 64)
    val applicationId = varchar("application_id", 64).references(ApplicationsTable.id, onDelete = ReferenceOption.CASCADE)
    val text = text("text")
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}
