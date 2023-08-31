package com.example.myapp.auth

import com.example.myapp.auth.util.HashUtil
import com.example.myapp.auth.util.JwtUtil
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.select
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.util.*

@Transactional
@RestController
@RequestMapping("/auth")
class AuthController(private val service: AuthService) {
    @PostMapping(value = ["/signup"])
    fun signUp(@RequestBody req: SignupRequest): ResponseEntity<Long> {
        println(req)

        // 1. Validation
        // 입력값 검증
        // 사용자이름없거나/중복이거나, 패스워드없거나, 닉네임, 이메일 없음...
        // 필수값은 SingupRequest에서 자동으로 검증

        // 2. Buisness Logic(데이터 처리)
        // profile, login 생성 트랜잭션 처리
        val profileId = service.createIdentity(req)

        // 3. Response
        // 201: created
        return ResponseEntity.status(HttpStatus.CREATED).body<Long>(profileId)
    }

    //1. (브라우저) 로그인 요청
    // [RequestLine]
    //   HTTP 1.1 POST 로그인주소
    // [RequestHeader]
    //   content-type: www-form-urlencoded
    // [Body]
    //   id=...&pw=...
    //2. (서버) 로그인 요청을 받고 인증처리 후 쿠키 응답 및 웹페이지로 이동
    // HTTP Status 302 (리다이렉트)
    // [Response Header]
    //   Set-Cookie: 인증키=키........; domain=.naver.com
    //   Location: "리다이렉트 주소"
    //3. (브라우저) 쿠키를 생성(도메인에 맞게)
    @Transactional(readOnly = true)
    @PostMapping(value = ["/signin"])
    fun signIn(
        @RequestParam username: String,
        @RequestParam password: String,
        res: HttpServletResponse
    ): ResponseEntity<*> {
        println(username)
        println(password)

        val i = Identities;
        val p = Profiles;

        // 1. username, pw 인증 확인
        //   1.1 username으로 login테이블에서 조회후 id, secret까지 조회
        var identityRecord = i.select(i.username eq username).singleOrNull()
            ?: return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(
                    ServletUriComponentsBuilder
                        .fromHttpUrl("http://localhost:5500/login.html?err=Unauthorized")
                        .build().toUri()
                )
                .build<Any>()
        // username에 매칭이 되는 레코드가 없는 상태

        //   1.2 password+salt -> 해시 -> secret 일치여부 확인
        //   1.3 일치하면 다음코드를 실행
        //   1.4 일치하지 않으면 401 Unauthorized 반환 종료
        val isVerified: Boolean = HashUtil.verifyHash(password, identityRecord[i.secret])
        if (!isVerified) {
            return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(
                    ServletUriComponentsBuilder
                        .fromHttpUrl("http://localhost:5500/login.html?err=Unauthorized")
                        .build().toUri()
                )
                .build<Any>()
        }

        // 2. profile 정보를 조회하여 인증키 생성(JWT)
        val profileRecord = p.select(p.identityId eq identityRecord[i.id].value).singleOrNull()
            ?: return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(
                    ServletUriComponentsBuilder
                        .fromHttpUrl("http://localhost:5500?err=Conflict")
                        .build().toUri()
                )
                .build<Any>();
        // 로그인정보와 프로필 정보가 제대로 연결 안됨.
        val token: String = JwtUtil.createToken(
            identityRecord[i.id].value,
            identityRecord[i.username],
            profileRecord[p.nickname]
        )
        println(token)

        // 3. cookie와 헤더를 생성한후 리다이렉트
        val cookie = Cookie("token", token)
        cookie.path = "/"
        cookie.maxAge = (JwtUtil.TOKEN_TIMEOUT / 1000L).toInt() // 만료시간
        cookie.domain = "localhost" // 쿠키를 사용할 수 있 도메인

        // 응답헤더에 쿠키 추가
        res.addCookie(cookie)

        // 웹 첫페이지로 리다이렉트
        return ResponseEntity
            .status(HttpStatus.FOUND)
            .location(
                ServletUriComponentsBuilder
                    .fromHttpUrl("http://localhost:5500")
                    .build().toUri()
            )
            .build<Any>()
    }
}
