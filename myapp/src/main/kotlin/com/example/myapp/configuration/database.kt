package com.example.myapp.configuration

import com.example.myapp.auth.Identities
import com.example.myapp.auth.Profiles
import com.example.myapp.post.PostComments
import com.example.myapp.post.Posts
import jakarta.annotation.PostConstruct
import org.jetbrains.exposed.spring.SpringTransactionManager
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.support.ResourceTransactionManager
import javax.sql.DataSource

@EnableTransactionManagement
@Configuration
class DatabaseConfiguration (val dataSource: DataSource) {

    @Bean
    fun databaseConfig(): DatabaseConfig {
        return DatabaseConfig {}
    }

    @Bean
    fun database(): Database {
        return Database.connect(dataSource)
    }

    @Value("\${spring.exposed.show-sql:false}")
    private var showSql: Boolean = false

    @Bean
    fun springTransactionManager(datasource: DataSource, databaseConfig: DatabaseConfig): SpringTransactionManager {
        return SpringTransactionManager(datasource, databaseConfig, showSql)
    }

}