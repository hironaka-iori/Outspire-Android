package com.computerization.outspire.data.repository

import com.computerization.outspire.data.model.DomainCasGroup
import com.computerization.outspire.data.model.DomainEvaluation
import com.computerization.outspire.data.model.DomainRecord
import com.computerization.outspire.data.model.DomainReflection
import com.computerization.outspire.data.model.LearningOutcome
import com.computerization.outspire.data.remote.CasService
import com.computerization.outspire.data.remote.dto.EvaluationDto
import com.computerization.outspire.data.remote.dto.EvaluationGroupDto
import com.computerization.outspire.data.remote.dto.GroupDto
import com.computerization.outspire.data.remote.dto.RecordDto
import com.computerization.outspire.data.remote.dto.ReflectionDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CasRepository @Inject constructor(
    private val service: CasService,
) {

    suspend fun myGroups(): Result<List<DomainCasGroup>> = runCatching {
        service.getMyGroups().map { it.toDomain() }
    }

    suspend fun allGroups(pageIndex: Int): Result<BrowsePage> = runCatching {
        val env = service.getAllGroups(pageIndex)
        BrowsePage(
            items = env.List.map { it.toDomain() },
            pageIndex = env.PageIndex,
            pageCount = env.PageCount.coerceAtLeast(1),
            totalCount = env.TotalCount,
        )
    }

    suspend fun join(groupId: String): Result<String> = runCatching { service.joinGroup(groupId) }

    suspend fun records(groupId: String): Result<List<DomainRecord>> = runCatching {
        service.getRecords(groupId).map { it.toDomain() }
    }

    suspend fun reflections(groupId: String): Result<List<DomainReflection>> = runCatching {
        service.getReflections(groupId).map { it.toDomain() }
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
    ): Result<Unit> = runCatching {
        service.saveRecord(id, groupId, activityDate, theme, cDuration, aDuration, sDuration, reflection)
    }

    suspend fun deleteRecord(id: String): Result<Unit> = runCatching { service.deleteRecord(id) }

    suspend fun saveReflection(
        id: String,
        groupId: String,
        title: String,
        summary: String,
        content: String,
        outcome: Int?,
    ): Result<Unit> = runCatching {
        service.saveReflection(id, groupId, title, summary, content, outcome)
    }

    suspend fun deleteReflection(id: String): Result<Unit> = runCatching { service.deleteReflection(id) }

    suspend fun evaluation(yearId: String): Result<DomainEvaluation> = runCatching {
        service.getEvaluation(yearId).toDomain()
    }

    data class BrowsePage(
        val items: List<DomainCasGroup>,
        val pageIndex: Int,
        val pageCount: Int,
        val totalCount: Int,
    ) {
        val hasMore: Boolean get() = pageIndex < pageCount
    }

    companion object {
        internal fun GroupDto.toDomain(): DomainCasGroup {
            val name = listOfNotNull(nameC?.trim()?.ifBlank { null }, nameE?.trim()?.ifBlank { null })
                .firstOrNull() ?: "Unnamed"
            val desc = listOfNotNull(descriptionC?.trim()?.ifBlank { null }, descriptionE?.trim()?.ifBlank { null })
                .firstOrNull().orEmpty()
            return DomainCasGroup(
                id = id,
                groupNo = groupNo.orEmpty(),
                name = name,
                teacher = teacherName.orEmpty(),
                description = desc,
            )
        }

        internal fun RecordDto.toDomain(): DomainRecord = DomainRecord(
            id = id,
            groupId = groupId,
            date = (activityDateStr ?: date).orEmpty().trim(),
            theme = (theme ?: title).orEmpty().trim(),
            cDuration = normalizeDuration(cDuration),
            aDuration = normalizeDuration(aDuration),
            sDuration = normalizeDuration(sDuration),
            reflection = reflection.orEmpty().trim(),
            confirmed = isConfirm == 1,
        )

        internal fun ReflectionDto.toDomain(): DomainReflection = DomainReflection(
            id = id,
            groupId = groupId,
            title = title.orEmpty().trim(),
            summary = summary.orEmpty().trim(),
            contentPreview = stripHtml(content.orEmpty()).take(200),
            outcome = LearningOutcome.from(outcome),
        )

        internal fun EvaluationDto.toDomain(): DomainEvaluation = DomainEvaluation(
            groups = groupRecordList.map { it.toRow() },
            recLevel = recLevel.orEmpty(),
            refLevel = refLevel.orEmpty(),
            talk = talk.orEmpty(),
            finalScore = finalScore.orEmpty(),
        )

        private fun EvaluationGroupDto.toRow(): DomainEvaluation.Row {
            val name = listOfNotNull(
                groupName?.trim()?.ifBlank { null },
                nameC?.trim()?.ifBlank { null },
                nameE?.trim()?.ifBlank { null },
            ).firstOrNull() ?: "—"
            return DomainEvaluation.Row(
                name = name,
                cDuration = normalizeDuration(cDuration),
                aDuration = normalizeDuration(aDuration),
                sDuration = normalizeDuration(sDuration),
            )
        }

        internal fun normalizeDuration(value: String?): String {
            val t = value?.trim().orEmpty()
            return if (t.isBlank() || t == "-") "0" else t
        }

        internal fun stripHtml(html: String): String =
            html.replace(Regex("<[^>]*>"), "")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace(Regex("\\s+"), " ")
                .trim()
    }
}
