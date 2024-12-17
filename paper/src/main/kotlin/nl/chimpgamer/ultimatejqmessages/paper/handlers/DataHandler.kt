package nl.chimpgamer.ultimatejqmessages.paper.handlers

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import nl.chimpgamer.ultimatejqmessages.paper.UltimateJQMessagesPlugin
import nl.chimpgamer.ultimatejqmessages.paper.storage.joinquitmessages.JoinQuitMessagesTable
import nl.chimpgamer.ultimatejqmessages.paper.storage.users.UsersTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

class DataHandler(private val ultimateTagsPlugin: UltimateJQMessagesPlugin) {
    private lateinit var database: Database

    val isDatabaseInitialized: Boolean get() = this::database.isInitialized

    private fun connect() {
        val databaseFile = File(ultimateTagsPlugin.dataFolder, "data.db")
        val settings = ultimateTagsPlugin.settingsConfig
        val storageType = settings.storageType.lowercase()

        if (storageType == "sqlite") {
            val hikariConfig = HikariConfig().apply {
                poolName = "UltimateJQMessages-pool"
                jdbcUrl = "jdbc:sqlite:${databaseFile.absolutePath}"
                driverClassName = "org.sqlite.JDBC"
                maximumPoolSize = 1
                transactionIsolation = "TRANSACTION_SERIALIZABLE"
            }
            database = Database.connect(HikariDataSource(hikariConfig), databaseConfig = DatabaseConfig {
                defaultMinRetryDelay = 100L
                keepLoadedReferencesOutOfTransaction = true
            })
        } else if (storageType == "mysql" || storageType == "mariadb") {
            val host = settings.storageHost
            val port = settings.storagePort
            val databaseName = settings.storageDatabase
            val username = settings.storageUsername
            val password = settings.storagePassword
            val properties = settings.storageProperties.toMutableMap()
            if (storageType == "mysql") {
                properties.apply {
                    putIfAbsent("cachePrepStmts", "true")
                    putIfAbsent("prepStmtCacheSize", "250")
                    putIfAbsent("prepStmtCacheSqlLimit", "2048")
                    putIfAbsent("useServerPrepStmts", "true")
                    putIfAbsent("useLocalSessionState", "true")
                    putIfAbsent("rewriteBatchedStatements", "true")
                    putIfAbsent("cacheResultSetMetadata", "true")
                    putIfAbsent("cacheServerConfiguration", "true")
                    putIfAbsent("elideSetAutoCommits", "true")
                    putIfAbsent("maintainTimeStats", "true")
                    putIfAbsent("alwaysSendSetIsolation", "false")
                    putIfAbsent("cacheCallableStmts", "true")
                }
            }

            var url = "jdbc:$storageType://$host:$port/$databaseName"
            if (properties.isNotEmpty()) {
                url += "?" + properties.map { "${it.key}=${it.value}" }.joinToString("&")
            }

            val hikariConfig = HikariConfig().apply {
                poolName = "UltimateJQMessages-pool"
                jdbcUrl = url
                driverClassName = if (storageType == "mysql") {
                    "com.mysql.cj.jdbc.Driver"
                } else {
                    "org.mariadb.jdbc.Driver"
                }
                this.username = username
                this.password = password
            }

            database = Database.connect(HikariDataSource(hikariConfig), databaseConfig = DatabaseConfig {
                keepLoadedReferencesOutOfTransaction = true
            })
        }
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