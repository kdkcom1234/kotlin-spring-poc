package com.example.myapp.configuration

import com.example.myapp.auth.Identities
import com.example.myapp.auth.Profiles
import com.example.myapp.post.PostComments
import com.example.myapp.post.Posts
import jakarta.annotation.PostConstruct
import org.jetbrains.exposed.spring.SpringTransactionManager
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.InitializingBean
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
    fun database(): Database {
        return Database.connect(dataSource)
    }

    @Bean
    fun transactionManager(dataSource: DataSource) =
        FixedSpringTransactionManager(SpringTransactionManager(dataSource))

    class FixedSpringTransactionManager(
        private val stm: SpringTransactionManager
    ) : ResourceTransactionManager by stm, InitializingBean by stm, TransactionManager by stm {
        override fun getTransaction(definition: TransactionDefinition?): TransactionStatus {
            TransactionManager.resetCurrent(this)
            return stm.getTransaction(definition)
        }
    }
}