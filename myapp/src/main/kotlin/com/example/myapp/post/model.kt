package com.example.myapp.post

import com.example.myapp.auth.Profiles
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

// Table을 정의
object Posts : Table("post") {
    val id = long("id").autoIncrement()
    val title = varchar("title", 100)
    val content = text("content")
    val createdDate  = datetime("created_date").defaultExpression(CurrentDateTime);
    override val primaryKey = PrimaryKey(id, name = "pk_post_id") // name is optional here
    val profileId = reference("profile_id", Profiles);
}

// LongIdTable: auto increment id가 있는 테이블을 정의
object PostComments : LongIdTable("post_comment") {
    val postId = reference("post_id", Posts.id)
    val comment = text("comment")
    val profileId = reference("profile_id", Profiles);
}

