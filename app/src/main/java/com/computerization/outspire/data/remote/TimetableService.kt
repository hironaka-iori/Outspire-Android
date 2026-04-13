package com.computerization.outspire.data.remote

import com.computerization.outspire.data.remote.dto.TimetableDto
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
class TimetableService @Inject constructor(
    private val client: HttpClient,
    private val authService: AuthService,
) {

    suspend fun getTimetableByStudent(yearId: Int, studentId: String): TimetableDto {
        return authService.withAuthRetry {
            val envelope: ApiEnvelope<TimetableDto> = client.post("/Stu/Timetable/GetTimetableByStudent") {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(FormDataContent(Parameters.build {
                    append("yearId", yearId.toString())
                    append("studentId", studentId)
                }))
            }.body()
            if (!envelope.isSuccess) {
                throw IllegalStateException("Timetable error: ${envelope.Message ?: "unknown"}")
            }
            envelope.Data ?: throw IllegalStateException("Timetable empty")
        }
    }
}
