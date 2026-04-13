package com.computerization.outspire.data.model

data class DomainCasGroup(
    val id: String,
    val groupNo: String,
    val name: String,
    val teacher: String,
    val description: String,
)

data class DomainRecord(
    val id: String,
    val groupId: String,
    val date: String,
    val theme: String,
    val cDuration: String,
    val aDuration: String,
    val sDuration: String,
    val reflection: String,
    val confirmed: Boolean,
)

data class DomainReflection(
    val id: String,
    val groupId: String,
    val title: String,
    val summary: String,
    val contentPreview: String,
    val outcome: LearningOutcome?,
)

data class DomainEvaluation(
    val groups: List<Row>,
    val recLevel: String,
    val refLevel: String,
    val talk: String,
    val finalScore: String,
) {
    data class Row(
        val name: String,
        val cDuration: String,
        val aDuration: String,
        val sDuration: String,
    )
}

enum class LearningOutcome(val code: Int, val label: String) {
    AWARENESS(1, "Awareness"),
    CHALLENGE(2, "Challenge"),
    INITIATIVE(3, "Initiative"),
    COLLABORATION(4, "Collaboration"),
    COMMITMENT(5, "Commitment"),
    GLOBAL_VALUE(6, "Global Value"),
    ETHICS(7, "Ethics"),
    NEW_SKILLS(8, "New Skills");

    companion object {
        fun from(code: Int?): LearningOutcome? = values().firstOrNull { it.code == code }
    }
}
