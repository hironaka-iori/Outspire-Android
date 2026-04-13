package com.computerization.outspire.data.repository

import com.computerization.outspire.data.model.LearningOutcome
import com.computerization.outspire.data.remote.ApiEnvelope
import com.computerization.outspire.data.remote.dto.EvaluationDto
import com.computerization.outspire.data.remote.dto.GroupDto
import com.computerization.outspire.data.remote.dto.PagedEnvelope
import com.computerization.outspire.data.remote.dto.RecordDto
import com.computerization.outspire.data.remote.dto.ReflectionDto
import com.computerization.outspire.data.repository.CasRepository.Companion.toDomain
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CasDtoTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `record uses Title when Theme is null and normalizes dash duration`() {
        val dto = RecordDto(
            id = "1",
            groupId = "g",
            title = "Cleanup",
            date = "2026-04-01",
            cDuration = "-",
            aDuration = " 2.5 ",
            sDuration = "",
            isConfirm = 1,
        )
        val domain = dto.toDomain()
        assertEquals("Cleanup", domain.theme)
        assertEquals("2026-04-01", domain.date)
        assertEquals("0", domain.cDuration)
        assertEquals("2.5", domain.aDuration)
        assertEquals("0", domain.sDuration)
        assertTrue(domain.confirmed)
    }

    @Test
    fun `record prefers Theme over Title and ActivityDateStr over Date`() {
        val dto = RecordDto(
            id = "2", groupId = "g",
            theme = "Workshop", title = "ignored",
            activityDateStr = "April 2", date = "ignored",
            cDuration = "1", aDuration = "1", sDuration = "1",
            isConfirm = 0,
        )
        val domain = dto.toDomain()
        assertEquals("Workshop", domain.theme)
        assertEquals("April 2", domain.date)
        assertFalse(domain.confirmed)
    }

    @Test
    fun `reflection maps outcome int to enum and strips html`() {
        val dto = ReflectionDto(
            id = "1", groupId = "g",
            title = "Week 1", summary = "good",
            content = "<p>Hello&nbsp;<b>world</b></p>",
            outcome = 3,
        )
        val d = dto.toDomain()
        assertEquals(LearningOutcome.INITIATIVE, d.outcome)
        assertEquals("Hello world", d.contentPreview)
    }

    @Test
    fun `reflection with null outcome yields null enum`() {
        assertNull(ReflectionDto(outcome = null).toDomain().outcome)
        assertNull(ReflectionDto(outcome = 99).toDomain().outcome)
    }

    @Test
    fun `paged envelope decodes from json`() {
        val raw = """
            {
              "ResultType": 0,
              "Data": {
                "PageIndex": 1,
                "PageSize": 20,
                "TotalCount": 101,
                "PageCount": 6,
                "List": [
                  {"Id":"31","NameC":"机器人","TeacherName":"Mr Wang"},
                  {"Id":"32","NameE":"Robotics","TeacherName":"Ms Li"}
                ]
              }
            }
        """.trimIndent()
        val env = json.decodeFromString(
            ApiEnvelope.serializer(PagedEnvelope.serializer(GroupDto.serializer())),
            raw,
        )
        assertTrue(env.isSuccess)
        val page = env.Data!!
        assertEquals(101, page.TotalCount)
        assertEquals(6, page.PageCount)
        assertEquals(2, page.List.size)
        assertEquals("机器人", page.List[0].toDomain().name)
        assertEquals("Robotics", page.List[1].toDomain().name)
    }

    @Test
    fun `my groups envelope decodes plain list`() {
        val raw = """
            {"ResultType":0,"Data":[{"Id":"7","NameC":"演讲","TeacherName":"X"}]}
        """.trimIndent()
        val env = json.decodeFromString(
            ApiEnvelope.serializer(ListSerializer(GroupDto.serializer())),
            raw,
        )
        val groups = env.Data!!.map { it.toDomain() }
        assertEquals(1, groups.size)
        assertEquals("演讲", groups[0].name)
    }

    @Test
    fun `evaluation dto decodes and maps`() {
        val raw = """
            {
              "ResultType": 0,
              "Data": {
                "GroupRecordList": [
                  {"GroupName":"Robotics","CDuration":"5","ADuration":"3","SDuration":"2"}
                ],
                "RecLevel":"A",
                "RefLevel":"B",
                "Talk":"4",
                "Final":"4.5"
              }
            }
        """.trimIndent()
        val env = json.decodeFromString(ApiEnvelope.serializer(EvaluationDto.serializer()), raw)
        val d = env.Data!!.toDomain()
        assertEquals("A", d.recLevel)
        assertEquals("4.5", d.finalScore)
        assertEquals(1, d.groups.size)
        assertEquals("Robotics", d.groups[0].name)
        assertEquals("5", d.groups[0].cDuration)
    }
}
