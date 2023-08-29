package com.example.todo

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

data class TodoCreateRequest(var memo: String);

@RestController
@RequestMapping(path = ["/todo"])
class TodoController (val repo: TodoRepository) {

    @GetMapping()
    fun fetchTodoList(@RequestParam page : Int, @RequestParam size : Int)
        = repo.findAll(PageRequest.of(page, size, Sort.by("id").descending()))

    @GetMapping(path = ["/search"])
    fun searchTodoList(@RequestParam page : Int, @RequestParam size : Int, @RequestParam keyword : String)
            = repo.findByMemoContains(keyword, PageRequest.of(page, size, Sort.by("id").ascending()))

    @PostMapping
    fun createTodo(@RequestBody req : TodoCreateRequest): ResponseEntity<Map<String, Any>> {
        println(req)
        val todo = repo.save(Todo(memo = req.memo))

        // https://medium.com/depayse/kotlin-collections-2-map-hashmap-treemap-linkedhashmap-76195842f0c8
        // getter만 사용가능
        // 불변 map
        var res = mapOf("message" to "Created", "data" to todo);

        return ResponseEntity.status(HttpStatus.CREATED).body(res)
    }

    @DeleteMapping(path = ["/{id}"] )
    fun removeTodo(@PathVariable id : Long) : ResponseEntity<Any> {
        println(id)

        if(!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        repo.deleteById(id);

        return ResponseEntity.ok().build();
    }
}