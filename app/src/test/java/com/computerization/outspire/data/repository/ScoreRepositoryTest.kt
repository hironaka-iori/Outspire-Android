package com.computerization.outspire.data.repository

import com.computerization.outspire.data.remote.ApiEnvelope
import com.computerization.outspire.data.remote.dto.ScoreItemDto
import com.computerization.outspire.data.repository.ScoreRepository.Companion.toDomain
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScoreRepositoryTest {

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false; isLenient = true }

    @Test
    fun `envelope with dash normalizes raw to zero and ib to blank`() {
        val payload = """
            {"ResultType":0,"Data":[
              {"SubjectId":1,"SubjectName":"Maths AA HL",
               "Score1":"70","IbScore1":"7",
               "Score2":"-","IbScore2":"-",
               "Score3":" 82 ","IbScore3":"6"}
            ]}
        """.trimIndent()
        val envelope = json.decodeFromString<ApiEnvelope<List<ScoreItemDto>>>(payload)
        assertTrue(envelope.isSuccess)
        val domain = envelope.Data!!.first().toDomain()
        assertEquals("Maths AA HL", domain.subject)
        assertEquals(3, domain.terms.size)
        assertEquals("70", domain.terms[0].raw)
        assertEquals("7", domain.terms[0].ib)
        assertEquals("0", domain.terms[1].raw)
        assertEquals("", domain.terms[1].ib)
        assertEquals("82", domain.terms[2].raw)
    }

    @Test
    fun `all empty trailing terms are dropped`() {
        val dto = ScoreItemDto(
            subjectName = "English",
            score1 = "80", ibScore1 = "6",
            score2 = null, ibScore2 = null,
            score3 = "-", ibScore3 = "-",
        )
        val domain = dto.toDomain()
        assertEquals(1, domain.terms.size)
        assertEquals("T1", domain.terms[0].label)
    }
}
