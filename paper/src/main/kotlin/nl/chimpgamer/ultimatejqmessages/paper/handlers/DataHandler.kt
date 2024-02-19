package nl.chimpgamer.ultimatejqmessages.paper.handlers

import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import nl.chimpgamer.ultimatejqmessages.paper.storage.joinquitmessages.JoinQuitMessagesTable
import nl.chimpgamer.ultimatejqmessages.paper.storage.users.UsersTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.sql.Connection

class DataHandler(private val ultimateTagsPlugin: UltimateJQMessagesPlugin) {
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
        if (isDatabaseInitialized) {
            transaction {
                //addLogger(StdOutSqlLogger)

                SchemaUtils.create(JoinQuitMessagesTable, UsersTable)
                SchemaUtils.createMissingTablesAndColumns(JoinQuitMessagesTable)
            }
        }
    }

    fun close() {
        TransactionManager.closeAndUnregister(database)
    }
}