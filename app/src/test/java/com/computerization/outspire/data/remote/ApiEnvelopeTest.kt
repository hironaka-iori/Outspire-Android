package com.computerization.outspire.data.remote

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiEnvelopeTest {

    @Serializable
    private data class Payload(val foo: String)

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false; isLenient = true }

    @Test
    fun `decodes success with int ResultType`() {
        val envelope = json.decodeFromString<ApiEnvelope<Payload>>(
            """{"ResultType":0,"Message":null,"Data":{"foo":"bar"}}"""
        )
        assertTrue(envelope.isSuccess)
        assertEquals("bar", envelope.Data?.foo)
    }

    @Test
    fun `decodes success with string ResultType`() {
        val envelope = json.decodeFromString<ApiEnvelope<Payload>>(
            """{"ResultType":"0","Data":{"foo":"baz"}}"""
        )
        assertTrue(envelope.isSuccess)
        assertEquals("baz", envelope.Data?.foo)
    }

    @Test
    fun `decodes failure with string ResultType`() {
        val envelope = json.decodeFromString<ApiEnvelope<Payload>>(
            """{"ResultType":"1","Message":"nope","Data":null}"""
        )
        assertFalse(envelope.isSuccess)
        assertEquals("nope", envelope.Message)
    }
}
