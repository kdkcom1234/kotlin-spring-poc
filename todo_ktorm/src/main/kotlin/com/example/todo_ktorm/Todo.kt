package com.example.todo_ktorm
import org.ktorm.schema.*

object Todo : Table<Nothing>("todo") {
    val id = long("id").primaryKey()
    val memo = varchar("memo");
    val done = boolean("done")
}