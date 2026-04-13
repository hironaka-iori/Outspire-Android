package com.computerization.outspire.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TsimsClientValidatorTest {

    private fun clientFor(engine: MockEngine): HttpClient = HttpClient(engine) {
        expectSuccess = false
        followRedirects = false
        HttpResponseValidator {
            validateResponse { resp ->
                val s = resp.status
                if (s == HttpStatusCode.Found || s == HttpStatusCode.Unauthorized) {
                    throw UnauthorizedException(s.value)
                }
            }
        }
    }

    @Test
    fun `302 response throws UnauthorizedException`() = runTest {
        val engine = MockEngine { respond("", HttpStatusCode.Found, headersOf("Location", "/Home/Login")) }
        val client = clientFor(engine)
        val err = runCatching { client.get("http://example/test") }.exceptionOrNull()
        assertTrue(err is UnauthorizedException)
        assertEquals(302, (err as UnauthorizedException).statusCode)
    }

    @Test
    fun `200 response passes through`() = runTest {
        val engine = MockEngine { respond("ok", HttpStatusCode.OK) }
        val client = clientFor(engine)
        val resp = client.get("http://example/test")
        assertEquals(HttpStatusCode.OK, resp.status)
    }
}
