package com.example.myapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

data class Person(val name: String, val age: Int)

@SpringBootApplication
class MyappApplication

fun main(args: Array<String>) {
	runApplication<MyappApplication>(*args)
}
