package com.rohit.jobtracker.backend.db

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.statements.jdbc.JdbcConnectionImpl
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object DatabaseFactory {

    fun init(
        driverClassName: String = "org.sqlite.JDBC",
        jdbcUrl: String = "jdbc:sqlite:data/jobtracker.db"
    ): Database {
        if (jdbcUrl.startsWith("jdbc:sqlite:") && !jdbcUrl.contains(":memory:")) {
            val dbPath = jdbcUrl.removePrefix("jdbc:sqlite:")
            val dbFile = File(dbPath)
            dbFile.parentFile?.mkdirs()
        }

        val database = Database.connect(
            url = jdbcUrl,
            driver = driverClassName
        )

        transaction(database) {
            val rawConnection = (TransactionManager.current().connection as? JdbcConnectionImpl)?.connection
            rawConnection?.createStatement()?.use { stmt ->
                stmt.execute("PRAGMA foreign_keys = ON;")
            }
            SchemaUtils.create(ApplicationsTable, NotesTable)
        }

        return database
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
