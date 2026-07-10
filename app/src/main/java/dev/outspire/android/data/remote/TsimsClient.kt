package dev.outspire.android.data.remote

import dev.outspire.android.data.model.ScheduleEntry
import dev.outspire.android.data.model.User
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.DayOfWeek
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

interface TsimsDataSource {
    val isConfigured: Boolean

    suspend fun login(code: String, password: String): Result<User>
    suspend fun logout()
    fun clearSession()
    suspend fun loadTimetable(user: User): Result<List<ScheduleEntry>>
}

internal fun isUnexpectedLoginRedirect(requestPath: String, responsePath: String): Boolean =
    !requestPath.isLoginPath() && responsePath.isLoginPath()

private fun String.isLoginPath(): Boolean =
    substringBefore('?').trimEnd('/').endsWith("/Home/Login", ignoreCase = true)

class TsimsClient(baseUrl: String) : TsimsDataSource {
    private val root = baseUrl.trim().trimEnd('/')
    private val cookies = CookieManager(null, CookiePolicy.ACCEPT_ALL).also { CookieHandler.setDefault(it) }

    override val isConfigured: Boolean get() = root.isNotBlank()

    override suspend fun login(code: String, password: String): Result<User> = runCatching {
        check(isConfigured) { "TSIMS is not configured. Add tsims.baseUrl to local.properties." }
        clearSession()
        seedSession()
        val json = postForm("/Home/Login", mapOf("code" to code, "password" to password))
        check(json.resultIsSuccess()) { json.optString("Message", "Login failed") }
        check(verifySession()) { "The server did not establish a valid session." }
        val data = json.optJSONObject("Data") ?: JSONObject()
        val loginUser = User(
            id = data.optIntOrNull("UserId"),
            code = data.optString("UserCode", code),
            name = data.optString("Name", code),
            role = data.optString("Role").ifBlank { null },
        )
        runCatching { fetchProfile(loginUser) }.getOrDefault(loginUser)
    }.onFailure { clearSession() }

    override suspend fun loadTimetable(user: User): Result<List<ScheduleEntry>> = runCatching {
        val yearId = fetchYearOptions().firstOrNull()?.first
            ?: error("No academic year was returned by TSIMS.")
        val form = buildMap {
            put("yearId", yearId)
            user.id?.let { put("studentId", it.toString()) }
        }
        val envelope = postForm("/Stu/Timetable/GetTimetableByStudent", form)
        check(envelope.resultIsSuccess()) { envelope.optString("Message", "Timetable request failed") }
        parseTimetable(envelope.opt("Data"))
    }

    override suspend fun logout() {
        try {
            if (isConfigured) postEmptyForm("/Home/logout")
        } catch (_: Exception) {
            // A local sign-out must still succeed if the server is unavailable.
        } finally {
            clearSession()
        }
    }

    override fun clearSession() {
        cookies.cookieStore.removeAll()
    }

    private suspend fun seedSession() = withContext(Dispatchers.IO) {
        val connection = open("/Home/Login?ReturnUrl=%2F", "GET", expectsJson = false)
        try {
            val status = connection.responseCode
            connection.readBody(status)
            check(status in 200..399) { "Unable to start a TSIMS sign-in session (HTTP $status)." }
        } finally {
            connection.disconnect()
        }
    }

    private suspend fun verifySession(): Boolean = withContext(Dispatchers.IO) {
        val connection = open("/Home/GetMenu", "GET")
        try {
            val status = connection.responseCode
            val body = connection.readBody(status)
            status == HttpURLConnection.HTTP_OK &&
                body.isNotBlank() &&
                !connection.landedOnLogin() &&
                !connection.contentType.orEmpty().contains("text/html", ignoreCase = true)
        } finally {
            connection.disconnect()
        }
    }

    private suspend fun fetchProfile(fallback: User): User = withContext(Dispatchers.IO) {
        val connection = open("/Home/StudentInfo", "GET", expectsJson = false)
        try {
            val status = connection.responseCode
            val html = connection.readBody(status)
            check(status in 200..299 && !connection.landedOnLogin()) {
                "Unable to load the signed-in TSIMS profile."
            }
            TsimsProfileParser.parse(html, fallback)
        } finally {
            connection.disconnect()
        }
    }

    private suspend fun fetchYearOptions(): List<Pair<String, String>> = withContext(Dispatchers.IO) {
        val connection = open("/Stu/Timetable/Index", "GET")
        try {
            check(connection.responseCode in 200..299) { "Unable to load academic years." }
            val html = connection.inputStream.bufferedReader().use { it.readText() }
            val select = Regex(
                """<select[^>]+(?:id|name)=[\"'][^\"']*Year[^\"']*[\"'][^>]*>(.*?)</select>""",
                setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
            ).find(html)?.groupValues?.get(1).orEmpty()
            Regex("""<option[^>]*value=[\"']([^\"']+)[\"'][^>]*>(.*?)</option>""", RegexOption.IGNORE_CASE)
                .findAll(select)
                .map { it.groupValues[1].trim() to it.groupValues[2].replace(Regex("<[^>]+>"), "").trim() }
                .filter { it.first.isNotBlank() }
                .toList()
        } finally {
            connection.disconnect()
        }
    }

    private suspend fun postForm(path: String, values: Map<String, String>): JSONObject =
        withContext(Dispatchers.IO) {
            val connection = open(path, "POST")
            val body = values.entries.joinToString("&") { (key, value) ->
                "${key.encode()}=${value.encode()}"
            }
            try {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                connection.outputStream.use { it.write(body.toByteArray(StandardCharsets.UTF_8)) }
                val status = connection.responseCode
                val text = connection.readBody(status)
                check(status in 200..299) { "TSIMS returned HTTP $status." }
                check(!isUnexpectedLoginRedirect(path, connection.url.path.orEmpty())) {
                    "TSIMS returned to the sign-in page. Check your credentials."
                }
                check(!connection.contentType.orEmpty().contains("text/html", ignoreCase = true)) {
                    "TSIMS returned an unexpected sign-in page."
                }
                check(text.isNotBlank()) { "TSIMS returned an empty response." }
                JSONObject(text)
            } finally {
                connection.disconnect()
            }
        }

    private suspend fun postEmptyForm(path: String) = withContext(Dispatchers.IO) {
        val connection = open(path, "POST")
        try {
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            connection.outputStream.use { }
            val status = connection.responseCode
            connection.readBody(status)
            check(status in 200..399) { "TSIMS returned HTTP $status while signing out." }
        } finally {
            connection.disconnect()
        }
    }

    private fun open(path: String, method: String, expectsJson: Boolean = true): HttpURLConnection {
        val url = URI.create(root + path).toURL()
        return (url.openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15_000
            readTimeout = 15_000
            instanceFollowRedirects = true
            if (expectsJson) {
                setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01")
                setRequestProperty("X-Requested-With", "XMLHttpRequest")
            } else {
                setRequestProperty("Accept", "text/html,application/xhtml+xml,*/*;q=0.8")
            }
            setRequestProperty("Referer", "$root/")
            setRequestProperty("Origin", root)
            setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
            setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/120.0 Mobile Safari/537.36",
            )
        }
    }

    private fun HttpURLConnection.readBody(status: Int): String {
        val stream = if (status in 200..399) {
            runCatching { inputStream }.getOrNull()
        } else {
            errorStream
        }
        return stream?.bufferedReader()?.use { it.readText() }.orEmpty()
    }

    private fun HttpURLConnection.landedOnLogin(): Boolean =
        url.path.orEmpty().contains("/Home/Login", ignoreCase = true)

    private fun parseTimetable(data: Any?): List<ScheduleEntry> {
        if (data is JSONArray) return parseSimpleArray(data)
        val rootObject = data as? JSONObject ?: return emptyList()
        val slots = rootObject.optJSONArray("TimetableList") ?: return emptyList()
        return buildList {
            for (slotIndex in 0 until slots.length()) {
                val lessons = slots.optJSONObject(slotIndex)?.optJSONArray("TimetableList") ?: continue
                for (index in 0 until lessons.length()) {
                    val lesson = lessons.optJSONObject(index) ?: continue
                    val day = dayFromNumber(lesson.optInt("WeekNumber")) ?: continue
                    add(
                        ScheduleEntry(
                            day = day,
                            period = lesson.optInt("LessonNumber"),
                            subject = lesson.optString("SubjectName", "Self-Study"),
                            room = lesson.optString("ClassRoomNo").ifBlank { null },
                            teacher = lesson.optString("TeacherName").ifBlank { null },
                        ),
                    )
                }
            }
        }
    }

    private fun parseSimpleArray(array: JSONArray): List<ScheduleEntry> = buildList {
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            val day = runCatching { DayOfWeek.valueOf(item.optString("Day").uppercase()) }.getOrNull() ?: continue
            add(
                ScheduleEntry(
                    day = day,
                    period = item.optInt("Period"),
                    subject = item.optString("Course", "Self-Study"),
                    room = item.optString("Room").ifBlank { null },
                    teacher = item.optString("Teacher").ifBlank { null },
                ),
            )
        }
    }

    private fun dayFromNumber(number: Int): DayOfWeek? = when (number) {
        1 -> DayOfWeek.MONDAY
        2 -> DayOfWeek.TUESDAY
        3 -> DayOfWeek.WEDNESDAY
        4 -> DayOfWeek.THURSDAY
        5 -> DayOfWeek.FRIDAY
        else -> null
    }

    private fun JSONObject.resultIsSuccess(): Boolean = when (val result = opt("ResultType")) {
        is Number -> result.toInt() == 0
        is String -> result == "0"
        else -> false
    }

    private fun JSONObject.optIntOrNull(key: String): Int? =
        if (has(key) && !isNull(key)) optInt(key) else null

    private fun String.encode(): String = URLEncoder.encode(this, StandardCharsets.UTF_8.name())
}
