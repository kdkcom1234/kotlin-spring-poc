package com.example.todo

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Entity
data class Todo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id : Long = 0,  // 불변(JPA에서는 어떻게 처리가 되는거지??)
    var memo: String = "",  // 변경가능
    var done: Boolean = false
)

@Repository
interface TodoRepository : JpaRepository<Todo, Long> {
    fun findByMemoContains(memo: String, pageable: Pageable): Page<Todo>
}
