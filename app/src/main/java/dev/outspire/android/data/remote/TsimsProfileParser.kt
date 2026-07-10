package dev.outspire.android.data.remote

import dev.outspire.android.data.model.User

internal object TsimsProfileParser {
    private val cellRegex = Regex(
        """<(?:td|th)\b[^>]*>(.*?)</(?:td|th)>""",
        setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
    )

    fun parse(html: String, fallback: User): User {
        val id = inputValue(html, "UserId", "StudentId", "id")?.toIntOrNull() ?: fallback.id
        val code = inputValue(html, "UserCode")
            ?: tableValue(html, "\u5b66\u53f7", "UserCode", "Student Code")
            ?: fallback.code

        val firstName = inputValue(html, "FirstName").orEmpty()
        val lastName = inputValue(html, "LastName").orEmpty()
        val combinedName = listOf(firstName, lastName)
            .filter { it.isNotBlank() }
            .joinToString(" ")
        val name = inputValue(html, "Name")
            ?: tableValue(html, "\u59d3\u540d", "Name")
            ?: combinedName.ifBlank { null }
            ?: fallback.name

        return fallback.copy(id = id, code = code, name = name)
    }

    private fun inputValue(html: String, vararg names: String): String? {
        for (name in names) {
            val escapedName = Regex.escape(name)
            val nameThenValue = Regex(
                """<input\b[^>]*\bname\s*=\s*["']$escapedName["'][^>]*\bvalue\s*=\s*["']([^"']*)["'][^>]*>""",
                RegexOption.IGNORE_CASE,
            )
            val valueThenName = Regex(
                """<input\b[^>]*\bvalue\s*=\s*["']([^"']*)["'][^>]*\bname\s*=\s*["']$escapedName["'][^>]*>""",
                RegexOption.IGNORE_CASE,
            )
            val value = nameThenValue.find(html)?.groupValues?.get(1)
                ?: valueThenName.find(html)?.groupValues?.get(1)
            cleanText(value.orEmpty()).takeIf { it.isNotBlank() }?.let { return it }
        }
        return null
    }

    private fun tableValue(html: String, vararg labels: String): String? {
        val cells = cellRegex.findAll(html)
            .map { cleanText(it.groupValues[1]) }
            .toList()
        val index = cells.indexOfFirst { cell ->
            labels.any { label -> cell.equals(label, ignoreCase = true) || cell.contains(label, ignoreCase = true) }
        }
        return cells.getOrNull(index + 1)?.takeIf { index >= 0 && it.isNotBlank() }
    }

    private fun cleanText(value: String): String = value
        .replace(Regex("<[^>]+>"), " ")
        .replace("&nbsp;", " ", ignoreCase = true)
        .replace("&amp;", "&", ignoreCase = true)
        .replace("&quot;", "\"", ignoreCase = true)
        .replace("&#39;", "'", ignoreCase = true)
        .replace("&lt;", "<", ignoreCase = true)
        .replace("&gt;", ">", ignoreCase = true)
        .replace(Regex("\\s+"), " ")
        .trim()
}
