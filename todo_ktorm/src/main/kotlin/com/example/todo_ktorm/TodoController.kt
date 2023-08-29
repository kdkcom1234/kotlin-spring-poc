package com.example.todo_ktorm

import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

data class TodoRes(val id: Long?, val memo: String?)
data class TodoCreateReq(val memo : String)

@RestController
@RequestMapping("/todos")
class TodoController(val database: Database) {

    @GetMapping
    fun fetchList() : List<TodoRes> {
        return database.from(Todo).select().map { r -> TodoRes(r[Todo.id], r[Todo.memo]) }
    }

    @GetMapping("/paging")
    fun fetchPagedList(@RequestParam page: Int, @RequestParam size : Int) : ResponseEntity<Map<String, Any>> {
        val query =  database
            .from(Todo)
            .select(Todo.id, Todo.memo)
            .limit(size).offset(page * size)

        val total = query.totalRecordsInAllPages
        val last = (page + 1) * size > total
        val data = query.map { r -> TodoRes(r[Todo.id], r[Todo.memo]) }

        return ResponseEntity.ok().body(mapOf("total" to total, "last" to last, "data" to data));
    }

    @PostMapping
    fun createTodo(@RequestBody req: TodoCreateReq) : ResponseEntity<Map<String, Any>> {
        val id = database.insertAndGenerateKey(Todo) {
            set(Todo.memo, req.memo)
        } as Long;

        val data = database.from(Todo).select().where{(Todo.id eq id)}

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(mapOf("message" to "created", "data" to data))
    }
}