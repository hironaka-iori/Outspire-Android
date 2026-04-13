package com.computerization.outspire.data.remote

import com.computerization.outspire.data.remote.dto.ScoreItemDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScoreService @Inject constructor(
    private val client: HttpClient,
    private val authService: AuthService,
) {

    suspend fun getScoreData(yearId: String): List<ScoreItemDto> {
        return authService.withAuthRetry {
            val envelope: ApiEnvelope<List<ScoreItemDto>> =
                client.post("/Stu/Exam/GetScoreData") {
                    contentType(ContentType.Application.FormUrlEncoded)
                    setBody(FormDataContent(Parameters.build {
                        append("yearId", yearId)
                        append("page", "1")
                        append("limit", "100")
                    }))
                }.body()
            if (!envelope.isSuccess) {
                throw IllegalStateException("Scores error: ${envelope.Message ?: "unknown"}")
            }
            envelope.Data.orEmpty()
        }
    }
}
