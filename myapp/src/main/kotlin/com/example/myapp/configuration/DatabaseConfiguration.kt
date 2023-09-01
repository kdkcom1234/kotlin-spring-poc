package com.example.myapp.configuration

import org.jetbrains.exposed.spring.SpringTransactionManager
import org.jetbrains.exposed.sql.*
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

@Configuration
@EnableTransactionManagement
class DatabaseConfiguration (val dataSource: DataSource) {
    @Bean
    fun database(): Database {
        return Database.connect(dataSource)
    }

    @Bean
    fun springTransactionManager(datasource: DataSource): SpringTransactionManager {
        return SpringTransactionManager(datasource)
    }
}