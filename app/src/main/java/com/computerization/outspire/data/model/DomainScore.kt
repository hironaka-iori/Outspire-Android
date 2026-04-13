package com.computerization.outspire.data.model

data class DomainScore(
    val subject: String,
    val terms: List<TermScore>,
) {
    data class TermScore(
        val label: String,
        val raw: String,
        val ib: String,
    )
}
