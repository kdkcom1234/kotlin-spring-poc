package com.example.myapp.auth

import com.example.myapp.auth.util.HashUtil
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.update
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService @Autowired constructor() {

    @Transactional
    fun createIdentity(req: SignupRequest): Long {
        // 1. login 정보를 insert
        val identityId = Identities.insertAndGetId {
            it[this.username] = req.username
            it[this.secret] = HashUtil.createHash(req.password)
        }

        // 2. profile 정보를 insert(login_id포함)
        val profileId = Profiles.insertAndGetId {
            it[this.nickname] = req.nickname
            it[this.email] = req.email
            it[this.identityId] = identityId.value
        }

        Identities.update {
            it[this.profileId] = profileId.value
        }

        // 4. profile_id를 반환
        return profileId.value
    }
}
