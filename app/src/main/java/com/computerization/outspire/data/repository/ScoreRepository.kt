package com.computerization.outspire.data.repository

import com.computerization.outspire.data.model.DomainScore
import com.computerization.outspire.data.remote.ScoreService
import com.computerization.outspire.data.remote.dto.ScoreItemDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScoreRepository @Inject constructor(
    private val service: ScoreService,
) {

    suspend fun scores(yearId: String): Result<List<DomainScore>> = runCatching {
        service.getScoreData(yearId).map { it.toDomain() }
    }

    companion object {
        internal fun ScoreItemDto.toDomain(): DomainScore {
            val raw = listOf(
                "T1" to (score1 to ibScore1),
                "T2" to (score2 to ibScore2),
                "T3" to (score3 to ibScore3),
                "T4" to (score4 to ibScore4),
                "T5" to (score5 to ibScore5),
            )
            val terms = raw.map { (label, pair) ->
                DomainScore.TermScore(
                    label = label,
                    raw = normalizeRaw(pair.first),
                    ib = normalizeIb(pair.second),
                )
            }
            val trimmed = terms.dropLastWhile { it.isEmpty() }
            return DomainScore(subject = subjectName.trim(), terms = trimmed)
        }

        private fun normalizeRaw(value: String?): String {
            val t = value?.trim().orEmpty()
            return if (t.isBlank() || t == "-") "0" else t
        }

        private fun normalizeIb(value: String?): String {
            val t = value?.trim().orEmpty()
            return if (t == "-") "" else t
        }

        private fun DomainScore.TermScore.isEmpty(): Boolean =
            (raw.isBlank() || raw == "0") && ib.isBlank()
    }
}
