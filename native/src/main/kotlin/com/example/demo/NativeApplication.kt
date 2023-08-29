package com.example.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class NativeApplication

fun main(args: Array<String>) {
	runApplication<NativeApplication>(*args)
}

@RestController
class SampleController {

	@GetMapping("/hello")
	fun hello() : String {
		return "Hello, Spring Native"
	}
}