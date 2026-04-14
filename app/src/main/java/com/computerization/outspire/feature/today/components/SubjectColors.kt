package com.computerization.outspire.feature.today.components

import androidx.compose.ui.graphics.Color

fun subjectColor(subject: String): Color {
    val s = subject.lowercase()
    val rules: List<Pair<Color, List<String>>> = listOf(
        Color(0xFF0A84FF).copy(alpha = 0.8f) to listOf("math", "mathematics", "maths"),
        Color(0xFF30D158).copy(alpha = 0.8f) to listOf("english", "language", "literature", "general paper", "esl"),
        Color(0xFFFF9F0A).copy(alpha = 0.8f) to listOf("physics", "science"),
        Color(0xFFBF5AF2).copy(alpha = 0.8f) to listOf("chemistry", "chem"),
        Color(0xFF64D2FF).copy(alpha = 0.8f) to listOf("biology", "bio"),
        Color(0xFF66D4CF).copy(alpha = 0.8f) to listOf("further math", "maths further"),
        Color(0xFFFFD60A).copy(alpha = 0.8f) to listOf("体育", "pe", "sports", "p.e"),
        Color(0xFFAC8E68).copy(alpha = 0.8f) to listOf("economics", "econ"),
        Color(0xFF5AC8FA).copy(alpha = 0.8f) to listOf("arts", "art", "tok"),
        Color(0xFF5E5CE6).copy(alpha = 0.8f) to listOf("chinese", "mandarin", "语文"),
        Color(0xFF8E8E93).copy(alpha = 0.8f) to listOf("history", "历史", "geography", "geo", "政治"),
    )
    rules.firstOrNull { (_, kws) -> kws.any { s.contains(it) } }?.let { return it.first }
    val hue = ((s.hashCode() and Int.MAX_VALUE) % 12) / 12f * 360f
    return Color.hsv(hue, 0.7f, 0.9f)
}
