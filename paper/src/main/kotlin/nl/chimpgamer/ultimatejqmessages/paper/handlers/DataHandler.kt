package nl.chimpgamer.ultimatetags.handlers

import nl.chimpgamer.ultimatetags.UltimateTagsPlugin
import nl.chimpgamer.ultimatetags.tables.UsersTable
import nl.chimpgamer.ultimatetags.tables.TagsTable
import nl.chimpgamer.ultimatetags.tables.UserTagsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.sql.Connection

class DataHandler(private val ultimateTagsPlugin: UltimateTagsPlugin) {
    private lateinit var database: Database

    val isDatabaseInitialized: Boolean get() = this::database.isInitialized

    private fun connect() {
        val databaseFile = File(ultimateTagsPlugin.dataFolder, "data.db")
        val databaseConfig = DatabaseConfig {
            keepLoadedReferencesOutOfTransaction = true
        }

        val settings = ultimateTagsPlugin.settingsConfig
        val databaseType = settings.storageType.lowercase()
        if (databaseType == "sqlite") {
            database = Database.connect("jdbc:sqlite:${databaseFile.absolutePath}", databaseConfig = databaseConfig)
        } else if (databaseType == "mysql" || databaseType == "mariadb") {
            val host = settings.storageHost
            val port = settings.storagePort
            val databaseName = settings.storageDatabase
            val username = settings.storageUsername
            val password = settings.storagePassword
            val properties = settings.storageProperties

            var url = "jdbc:$databaseType://$host:$port/$databaseName"
            if (properties.isNotEmpty()) {
               url += "?" + properties.map { "${it.key}=${it.value}" }.joinToString("&")
            }

            database = Database.connect(
                url,
                user = username,
                password = password,
                databaseConfig = databaseConfig
            )
        }
        if (isDatabaseInitialized) TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
    }

    fun initialize() {
        connect()
        transaction {
            //addLogger(StdOutSqlLogger)

            SchemaUtils.create(TagsTable, UsersTable, UserTagsTable)
        }
    }

    fun close() {
        TransactionManager.closeAndUnregister(database)
    }
}