package com.example.myapp.post

import com.example.myapp.auth.Auth
import com.example.myapp.auth.AuthProfile
import com.example.myapp.auth.Profiles
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.sql.Connection
import java.time.LocalDateTime

@RequestMapping("posts")
@RestController
class PostController() {

    @GetMapping
    fun fetch(): List<PostResponse>
        = transaction(Connection.TRANSACTION_READ_UNCOMMITTED, readOnly = true) {
        Posts.selectAll()
        .map { r -> PostResponse(r[Posts.id],  r[Posts.title], r[Posts.content], r[Posts.createdDate].toString()) }
    }

    @GetMapping("/paging")
    fun paging(@RequestParam size : Int, @RequestParam page : Int) : Page<PostResponse>
        = transaction(Connection.TRANSACTION_READ_UNCOMMITTED, readOnly = true) {
        // 페이징 조회
        val content = Posts.selectAll()
            .orderBy(Posts.id to SortOrder.DESC).limit(size, offset= (size * page).toLong())
            .map { r -> PostResponse(r[Posts.id],  r[Posts.title], r[Posts.content], r[Posts.createdDate].toString()) }

        // 전체 결과 카운트
        val totalCount = Posts.selectAll().count();

        // Page 객체로 리턴
        return@transaction PageImpl(content, PageRequest.of(page, size),  totalCount);
    }


    @GetMapping("/paging/search")
    fun searchPaging(@RequestParam size : Int, @RequestParam page : Int, @RequestParam keyword : String?) : Page<PostResponse>
        = transaction(Connection.TRANSACTION_READ_UNCOMMITTED, readOnly = true) {
        val query = when {
            keyword != null -> Posts.select((Posts.title like "%${keyword}%") or (Posts.content like "%${keyword}%" ))
            else -> Posts.selectAll()
        }

        // 페이징 조회
        val content = query
            .orderBy(Posts.id to SortOrder.DESC).limit(size, offset= (size * page).toLong())
            .map { r ->
                PostResponse(r[Posts.id],
                    r[Posts.title],
                    r[Posts.content], r[Posts.createdDate].toString())
            }

        // 전체 결과 카운트
        val totalCount = query.count();

        // Page 객체로 리턴
        return@transaction PageImpl(content, PageRequest.of(page, size),  totalCount);
    }

    @GetMapping("/commentCount")
    fun fetchCommentCount(@RequestParam size : Int, @RequestParam page : Int,
                          @RequestParam keyword : String?) : Page<PostCommentCountResponse>
        = transaction(Connection.TRANSACTION_READ_UNCOMMITTED, readOnly = true) {
        // 단축 이름 변수 사용
        val pf = Profiles;
        val p = Posts;
        val c = PostComments;

        val commentCount = PostComments.id.count();

        // 조인 및 특정 컬럼 선택 및 count함수 사용
        val slices = ((p innerJoin pf) leftJoin c)
            .slice(p.id, p.title, p.createdDate, p.profileId, pf.nickname, commentCount);

        // 검색 조건 설정
        val query = when {
            keyword != null -> slices.select((Posts.title like "%${keyword}%") or (Posts.content like "%${keyword}%" ))
            else -> slices.selectAll()
        }

        // 페이징 조회
        val content = query
            .groupBy(p.id, p.title, p.profileId, pf.nickname)
            .orderBy(p.id to SortOrder.DESC).limit(size, offset= (size * page).toLong())
            .map { r -> PostCommentCountResponse(r[p.id],  r[p.title], r[p.createdDate].toString(), r[p.profileId].value, r[pf.nickname], r[commentCount]) }

        // 전체 결과 카운트
        val totalCount = query.count();

        // Page 객체로 리턴
        return@transaction PageImpl(content, PageRequest.of(page, size), totalCount);
    }

    @Auth
    @PostMapping
    fun create(@RequestBody request : PostCreateRequest, @RequestAttribute authProfile: AuthProfile? ) : ResponseEntity<Map<String, Any?>> {
        println(authProfile)
        if(authProfile == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .build()
        }

        if(request.title.isEmpty() || request.content.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("message" to "title and content is required"))
        }

        // 트랜잭션 시작
        val (result, response) = transaction {
            val result = Posts.insert {
                it[title] = request.title
                it[content] = request.content
                it[createdDate] = LocalDateTime.now();
                it[profileId] = authProfile.id
            }.resultedValues
                ?: return@transaction Pair(false, null)

            val record = result.first()
            return@transaction Pair(true, PostResponse(
                record[Posts.id],
                record[Posts.title],
                record[Posts.content],
                record[Posts.createdDate].toString()
            ))
        }

        if(result) {
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapOf("data" to response, "message" to "created"))
        }

        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(mapOf("data" to response, "message" to "conflict"))
    }

    @DeleteMapping("/{id}")
    fun remove(@PathVariable id : Long): ResponseEntity<Any> {
        if(  transaction { Posts.select( Posts.id eq id ).firstOrNull() }  == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        transaction {
            Posts.deleteWhere { Posts.id eq id }
        }

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    fun modify(@PathVariable id : Long, @RequestBody request: PostModifyRequest): ResponseEntity<Any> {
        if(request.title.isNullOrEmpty() && request.content.isNullOrEmpty()) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(mapOf("message" to "title or content is required"))
        }

        transaction { Posts.select( Posts.id eq id ).firstOrNull() }
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        transaction { Posts.update({ Posts.id eq id }) {
                if(!request.title.isNullOrEmpty()) {
                    it[this.title] = request.title
                }
                if(!request.content.isNullOrEmpty()) {
                    it[this.content] = request.content
                }
            }
        }

        return ResponseEntity.ok().build();
    }
}