package com.computerization.outspire.data.remote

import com.computerization.outspire.data.remote.dto.EvaluationDto
import com.computerization.outspire.data.remote.dto.GroupDto
import com.computerization.outspire.data.remote.dto.PagedEnvelope
import com.computerization.outspire.data.remote.dto.RecordDto
import com.computerization.outspire.data.remote.dto.ReflectionDto
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
class CasService @Inject constructor(
    private val client: HttpClient,
    private val authService: AuthService,
) {

    suspend fun getMyGroups(): List<GroupDto> = authService.withAuthRetry {
        val env: ApiEnvelope<List<GroupDto>> = client.post("/Stu/Cas/GetMyGroupList") {
            form(mapOf("categoryId" to ""))
        }.body()
        env.require("GetMyGroupList").orEmpty()
    }

    suspend fun getAllGroups(pageIndex: Int, pageSize: Int = 20): PagedEnvelope<GroupDto> =
        authService.withAuthRetry {
            val env: ApiEnvelope<PagedEnvelope<GroupDto>> = client.post("/Stu/Cas/GetGroupList") {
                form(
                    mapOf(
                        "pageIndex" to pageIndex.toString(),
                        "pageSize" to pageSize.toString(),
                        "categoryId" to "",
                    )
                )
            }.body()
            env.require("GetGroupList") ?: PagedEnvelope()
        }

    suspend fun joinGroup(groupId: String): String = authService.withAuthRetry {
        val env: ApiEnvelope<String> = client.post("/Stu/Cas/JoinGroup") {
            form(mapOf("groupId" to groupId))
        }.body()
        if (!env.isSuccess) throw IllegalStateException(env.Message ?: "Join failed")
        env.Message ?: "Joined"
    }

    suspend fun getRecords(groupId: String): List<RecordDto> = authService.withAuthRetry {
        val env: ApiEnvelope<PagedEnvelope<RecordDto>> = client.post("/Stu/Cas/GetRecordList") {
            form(
                mapOf(
                    "pageIndex" to "1",
                    "pageSize" to "100",
                    "groupId" to groupId,
                )
            )
        }.body()
        env.require("GetRecordList")?.List.orEmpty()
    }

    suspend fun getReflections(groupId: String): List<ReflectionDto> = authService.withAuthRetry {
        val env: ApiEnvelope<PagedEnvelope<ReflectionDto>> =
            client.post("/Stu/Cas/GetReflectionList") {
                form(
                    mapOf(
                        "pageIndex" to "1",
                        "pageSize" to "100",
                        "groupId" to groupId,
                    )
                )
            }.body()
        env.require("GetReflectionList")?.List.orEmpty()
    }

    suspend fun saveRecord(
        id: String,
        groupId: String,
        activityDate: String,
        theme: String,
        cDuration: String,
        aDuration: String,
        sDuration: String,
        reflection: String,
    ) = authService.withAuthRetry {
        val env: ApiEnvelope<String> = client.post("/Stu/Cas/SaveRecord") {
            form(
                mapOf(
                    "id" to id.ifBlank { "0" },
                    "GroupId" to groupId,
                    "ActivityDate" to activityDate,
                    "Theme" to theme,
                    "CDuration" to cDuration,
                    "ADuration" to aDuration,
                    "SDuration" to sDuration,
                    "Reflection" to reflection,
                )
            )
        }.body()
        if (!env.isSuccess) throw IllegalStateException(env.Message ?: "Save failed")
    }

    suspend fun deleteRecord(id: String) = authService.withAuthRetry {
        val env: ApiEnvelope<String> = client.post("/Stu/Cas/DeleteRecord") {
            form(mapOf("id" to id))
        }.body()
        if (!env.isSuccess) throw IllegalStateException(env.Message ?: "Delete failed")
    }

    suspend fun saveReflection(
        id: String,
        groupId: String,
        title: String,
        summary: String,
        content: String,
        outcome: Int?,
    ) = authService.withAuthRetry {
        val fields = mutableMapOf(
            "id" to id.ifBlank { "0" },
            "GroupId" to groupId,
            "Title" to title,
            "Summary" to summary,
            "Content" to content,
        )
        if (outcome != null) fields["outcome"] = outcome.toString()
        val env: ApiEnvelope<String> = client.post("/Stu/Cas/SaveReflection") {
            form(fields)
        }.body()
        if (!env.isSuccess) throw IllegalStateException(env.Message ?: "Save failed")
    }

    suspend fun deleteReflection(id: String) = authService.withAuthRetry {
        val env: ApiEnvelope<String> = client.post("/Stu/Cas/DeleteReflection") {
            form(mapOf("id" to id))
        }.body()
        if (!env.isSuccess) throw IllegalStateException(env.Message ?: "Delete failed")
    }

    suspend fun getEvaluation(yearId: String): EvaluationDto = authService.withAuthRetry {
        val env: ApiEnvelope<EvaluationDto> = client.post("/Stu/Cas/GetEvaluateData") {
            form(mapOf("yearId" to yearId))
        }.body()
        env.require("GetEvaluateData") ?: EvaluationDto()
    }
}

private fun io.ktor.client.request.HttpRequestBuilder.form(fields: Map<String, String>) {
    contentType(ContentType.Application.FormUrlEncoded)
    setBody(FormDataContent(Parameters.build { fields.forEach { (k, v) -> append(k, v) } }))
}

private fun <T> ApiEnvelope<T>.require(op: String): T? {
    if (!isSuccess) throw IllegalStateException("$op error: ${Message ?: "unknown"}")
    return Data
}
