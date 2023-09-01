package com.example.myapp.auth

import com.example.myapp.auth.util.HashUtil
import com.example.myapp.auth.util.JwtUtil
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.transactionManager
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.sql.Connection

@Service
class AuthService(private val database: Database){
    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    fun createIdentity(req: SignupRequest) : Long {
        val secret = HashUtil.createHash(req.password)

        val profileId = transaction {
            try {
                // 1. identity 정보를 insert
                val identityId = Identities.insertAndGetId {
                    it[this.username] = req.username
                    it[this.secret] = secret
                }

                // 2. profile 정보를 insert(identity_id포함)
                val profileId = Profiles.insertAndGetId {
                    it[this.nickname] = req.nickname
                    it[this.email] = req.email
                    it[this.identityId] = identityId.value
                }

                // 3. identity 테이블에 profile id를 업데이트
                Identities.update {
//                    it[this.profileId] = 0
                    it[this.profileId] = profileId.value
                }

                return@transaction profileId.value
            } catch (e: Exception) {
                rollback()
                logger.error(e.message)
                return@transaction 0
            }
        }

        return profileId
    }

    fun authenticate(username: String, password: String) : Pair<Boolean, String> =
        transaction(Connection.TRANSACTION_NONE, readOnly = true) {
            val i = Identities;
            val p = Profiles;

            // 1. username, pw 인증 확인
            //   1.1 username으로 login테이블에서 조회후 id, secret까지 조회
            // username에 매칭이 되는 레코드가 없는 상태
            var identityRecord = i.select(i.username eq username).singleOrNull()
                ?: return@transaction Pair(false, "Unauthorized")


            //   1.2 password+salt -> 해시 -> secret 일치여부 확인
            //   1.3 일치하면 다음코드를 실행
            //   1.4 일치하지 않으면 401 Unauthorized 반환 종료
            val isVerified = HashUtil.verifyHash(password, identityRecord[i.secret])
            if (!isVerified) {
                return@transaction Pair(false, "Unauthorized")
            }

            // 2. profile 정보를 조회하여 인증키 생성(JWT)
            // 로그인정보와 프로필 정보가 제대로 연결 안됨.
            val profileRecord = p.select(p.identityId eq identityRecord[i.id].value).singleOrNull()
                ?: return@transaction Pair(false, "Conflict")

            // 토큰 생성
            val token = JwtUtil.createToken(
                identityRecord[i.id].value,
                identityRecord[i.username],
                profileRecord[p.nickname]
            )

            return@transaction Pair(true, token)
    }

}
