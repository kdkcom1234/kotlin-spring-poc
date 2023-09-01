package com.example.myapp.post

data class PostResponse(val id : Long, val title : String, val content: String, val createdDate: String)
data class PostCommentCountResponse(
    val id : Long,
    val title : String,
    val createdDate: String,
    val profileId : Long,
    val nickname: String,
    val commentCount : Long
)
data class PostCreateRequest(val title : String, val content: String)
data class PostModifyRequest(val title : String?, val content: String?)
