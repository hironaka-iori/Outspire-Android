package dev.outspire.android.data.model

data class SubjectScore(
    val subject: String,
    val term: String,
    val score: String,
    val grade: String? = null,
)
