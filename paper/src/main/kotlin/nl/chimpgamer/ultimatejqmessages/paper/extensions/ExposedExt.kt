package nl.chimpgamer.ultimatejqmessages.paper.extensions

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.BatchInsertStatement
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.TransactionManager

class BatchInsertUpdateOnDuplicate(table: Table, val onDupUpdate: List<Column<*>>) : BatchInsertStatement(table, false) {
    override fun prepareSQL(transaction: Transaction): String {
        val onUpdateSQL = if (onDupUpdate.isNotEmpty()) {
            " ON DUPLICATE KEY UPDATE " + onDupUpdate.joinToString { "${transaction.identity(it)}=VALUES(${transaction.identity(it)})" }
        } else ""
        return super.prepareSQL(transaction) + onUpdateSQL
    }
}

class InsertUpdateOnDuplicate(table: Table, val onDupUpdate: List<Column<*>>) : InsertStatement<ResultRow>(table, false) {
    override fun prepareSQL(transaction: Transaction): String {
        val onUpdateSQL = if (onDupUpdate.isNotEmpty()) {
            " ON DUPLICATE KEY UPDATE " + onDupUpdate.joinToString { "${transaction.identity(it)}=VALUES(${transaction.identity(it)})" }
        } else ""
        return super.prepareSQL(transaction) + onUpdateSQL
    }
}

fun <T : Table, E> T.batchInsertOnDuplicateKeyUpdate(data: List<E>, onDupUpdateColumns: List<Column<*>>, body: T.(BatchInsertUpdateOnDuplicate, E) -> Unit) {
    data.
    takeIf { it.isNotEmpty() }?.
    let {
        val insert = BatchInsertUpdateOnDuplicate(this, onDupUpdateColumns)
        data.forEach {
            insert.addBatch()
            body(insert, it)
        }
        TransactionManager.current().exec(insert)
    }
}

fun <T : Table, E> T.insertOnDuplicateKeyUpdate(data: E, onDupUpdateColumns: List<Column<*>>, body: T.(InsertUpdateOnDuplicate, E) -> Unit) {
    data?.let {
        val insert = InsertUpdateOnDuplicate(this, onDupUpdateColumns)
        body(insert, data)
        TransactionManager.current().exec(insert)
    }
}