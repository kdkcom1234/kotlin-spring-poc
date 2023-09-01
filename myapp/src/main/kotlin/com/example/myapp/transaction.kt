package com.example.myapp

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

fun <T> transactionUncommitedReadOnly(
    statement: Transaction.() -> T
): Transaction.() -> T =
    transaction ( Connection.TRANSACTION_READ_UNCOMMITTED, readOnly = true) { statement }
