package com.computerization.outspire.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScoreItemDto(
    @SerialName("SubjectId") val subjectId: Int = 0,
    @SerialName("SubjectName") val subjectName: String = "",
    @SerialName("Score1") val score1: String? = null,
    @SerialName("IbScore1") val ibScore1: String? = null,
    @SerialName("Score2") val score2: String? = null,
    @SerialName("IbScore2") val ibScore2: String? = null,
    @SerialName("Score3") val score3: String? = null,
    @SerialName("IbScore3") val ibScore3: String? = null,
    @SerialName("Score4") val score4: String? = null,
    @SerialName("IbScore4") val ibScore4: String? = null,
    @SerialName("Score5") val score5: String? = null,
    @SerialName("IbScore5") val ibScore5: String? = null,
    @SerialName("ScoreH") val scoreH: String? = null,
    @SerialName("IbScoreH") val ibScoreH: String? = null,
    @SerialName("ScoreF") val scoreF: String? = null,
    @SerialName("IbScoreF") val ibScoreF: String? = null,
)
