package com.rohit.jobtracker.backend.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.statements.jdbc.JdbcConnectionImpl
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.net.URI

object DatabaseFactory {

    fun init(jdbcUrl: String = "jdbc:sqlite:data/jobtracker.db"): Database {
        val trimmed = jdbcUrl.trim()
        val isPostgres = trimmed.startsWith("postgres://", ignoreCase = true) ||
                trimmed.startsWith("postgresql://", ignoreCase = true) ||
                trimmed.startsWith("jdbc:postgresql:", ignoreCase = true)

        val database = if (isPostgres) {
            val dataSource = createPostgresDataSource(trimmed)
            Database.connect(dataSource)
        } else {
            if (trimmed.startsWith("jdbc:sqlite:") && !trimmed.contains(":memory:")) {
                val dbPath = trimmed.removePrefix("jdbc:sqlite:")
                val dbFile = File(dbPath)
                dbFile.parentFile?.mkdirs()
            }
            Database.connect(url = trimmed, driver = "org.sqlite.JDBC")
        }

        transaction(database) {
            if (!isPostgres) {
                val rawConnection = (TransactionManager.current().connection as? JdbcConnectionImpl)?.connection
                rawConnection?.createStatement()?.use { stmt ->
                    stmt.execute("PRAGMA foreign_keys = ON;")
                }
            }
            SchemaUtils.create(ApplicationsTable, NotesTable)
        }

        return database
    }

    private fun createPostgresDataSource(rawUrl: String): HikariDataSource {
        val config = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 5
            minimumIdle = 1
            idleTimeout = 30000
            maxLifetime = 600000
            connectionTimeout = 10000
            isAutoCommit = false

            if (rawUrl.startsWith("jdbc:postgresql:", ignoreCase = true)) {
                jdbcUrl = rawUrl
            } else {
                val uri = URI(rawUrl)
                val userInfo = uri.userInfo?.split(":") ?: emptyList()
                val username = userInfo.getOrNull(0)
                val password = userInfo.getOrNull(1)

                val host = uri.host
                val port = if (uri.port > 0) uri.port else 5432
                val dbPath = uri.path.trimStart('/')
                val rawQuery = uri.query

                var constructedUrl = "jdbc:postgresql://$host:$port/$dbPath"
                if (!rawQuery.isNullOrBlank()) {
                    constructedUrl += "?$rawQuery"
                    if (!constructedUrl.contains("sslmode=", ignoreCase = true)) {
                        constructedUrl += "&sslmode=require"
                    }
                } else {
                    constructedUrl += "?sslmode=require"
                }

                this.jdbcUrl = constructedUrl
                if (!username.isNullOrBlank()) this.username = username
                if (!password.isNullOrBlank()) this.password = password
            }
        }
        return HikariDataSource(config)
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
