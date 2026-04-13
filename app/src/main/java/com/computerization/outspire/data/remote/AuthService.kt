package com.computerization.outspire.data.remote

import com.computerization.outspire.data.local.SecureCredentialStore
import com.computerization.outspire.data.remote.dto.StudentProfileDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.contentType
import org.jsoup.Jsoup
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthService @Inject constructor(
    private val client: HttpClient,
    private val store: SecureCredentialStore,
    private val cookieJar: PersistentCookieJar,
) {

    suspend fun logout() {
        cookieJar.clear()
        store.clear()
    }

    suspend fun login(username: String, password: String): StudentProfileDto {
        runCatching { client.get("/Home/Login") { url.parameters.append("ReturnUrl", "/") } }

        val loginResp = client.post("/Home/Login") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(FormDataContent(Parameters.build {
                append("code", username)
                append("password", password)
            }))
        }
        if (loginResp.status != HttpStatusCode.OK && loginResp.status != HttpStatusCode.Found) {
            throw IllegalStateException("Login failed: HTTP ${loginResp.status.value}")
        }

        val menuResp = client.get("/Home/GetMenu")
        if (menuResp.status != HttpStatusCode.OK) {
            throw IllegalStateException("Session verify failed: HTTP ${menuResp.status.value}")
        }
        val menuBody = menuResp.bodyAsText()
        if (menuBody.isBlank()) {
            throw IllegalStateException("Session verify failed: empty menu")
        }

        val profile = fetchProfile()

        store.username = username
        store.password = password
        store.studentId = profile.studentId
        return profile
    }

    suspend fun fetchProfile(): StudentProfileDto {
        val html = client.get("/Home/StudentInfo").bodyAsText()
        val doc = Jsoup.parse(html)
        val studentId = doc.select("input[name=id]").attr("value").takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("StudentInfo missing id")
        val userCode = doc.select("input[name=UserCode]").attr("value").ifBlank { null }
        val firstName = doc.select("input[name=FirstName]").attr("value").ifBlank { null }
        val lastName = doc.select("input[name=LastName]").attr("value").ifBlank { null }
        return StudentProfileDto(studentId, userCode, firstName, lastName)
    }

    suspend fun <T> withAuthRetry(block: suspend () -> T): T {
        return try {
            block()
        } catch (_: UnauthorizedException) {
            val user = store.username ?: throw IllegalStateException("No stored credentials")
            val pass = store.password ?: throw IllegalStateException("No stored credentials")
            login(user, pass)
            block()
        }
    }
}
