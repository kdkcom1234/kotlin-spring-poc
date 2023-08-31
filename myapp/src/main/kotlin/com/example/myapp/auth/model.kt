package com.example.myapp.auth

import com.example.myapp.auth.Profiles.nullable
import org.jetbrains.exposed.dao.id.LongIdTable

object Identities : LongIdTable("identity") {
    val secret = varchar("secret", 200)
    val username = varchar("username", length = 100)
    val profileId = reference("profile_id", Profiles).nullable()
}

object Profiles : LongIdTable("profile") {
    val email = varchar("email", 200)
    val nickname = varchar("nickname", 100)
    val identityId = reference("identity_id", Identities )
}