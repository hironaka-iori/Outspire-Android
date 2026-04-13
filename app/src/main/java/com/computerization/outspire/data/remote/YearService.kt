package com.computerization.outspire.data.remote

import com.computerization.outspire.data.remote.dto.YearOption
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import org.jsoup.nodes.Document
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YearService @Inject constructor(
    private val client: HttpClient,
    private val authService: AuthService,
) {

    suspend fun fetchYearOptions(): List<YearOption> = authService.withAuthRetry {
        val html = client.get("/Stu/Timetable/Index").bodyAsText()
        parseYearOptions(org.jsoup.Jsoup.parse(html))
    }

    companion object {
        private val SELECTORS = listOf(
            "select#YearId option",
            "select#ddlYear option",
            "select[id*=Year] option",
            "select[name*=Year] option",
        )

        internal fun parseYearOptions(doc: Document): List<YearOption> {
            for (sel in SELECTORS) {
                val options = doc.select(sel)
                if (options.isNotEmpty()) {
                    return options.mapNotNull { el ->
                        val value = el.attr("value").trim()
                        val label = el.text().trim()
                        if (value.isBlank() || label.isBlank()) null
                        else YearOption(id = value, name = label)
                    }
                }
            }
            throw IllegalStateException("No year select found in /Stu/Timetable/Index")
        }
    }
}
