package dev.ag6.libredesktop.api

import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class LibreApiResponseSerializerTest {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun decodesSuccessEnvelope() {
        val response = decodeLibreApiResponse(
            """{"status":0,"data":42}""",
            Int.serializer(),
            json
        )

        val success = assertIs<LibreApiResponse.Success<Int>>(response)
        assertEquals(0, success.status)
        assertEquals(42, success.data)
    }

    @Test
    fun decodesRedirectEnvelope() {
        val response = decodeLibreApiResponse(
            """{"status":307,"redirect":true,"region":"eu"}""",
            Int.serializer(),
            json
        )

        val redirect = assertIs<LibreApiResponse.Redirect>(response)
        assertEquals(307, redirect.status)
        assertEquals("eu", redirect.region)
    }

    @Test
    fun decodesMessageErrorEnvelope() {
        val response = decodeLibreApiResponse(
            """{"status":401,"message":{"message":"Unauthorized"}}""",
            Int.serializer(),
            json
        )

        val error = assertIs<LibreApiResponse.Error>(response)
        assertEquals(401, error.status)
        assertEquals("Unauthorized", error.message?.message)
    }

    @Test
    fun decodesErrorFieldEnvelope() {
        val response = decodeLibreApiResponse(
            """{"status":401,"error":{"message":"Unauthorized"}}""",
            Int.serializer(),
            json
        )

        val error = assertIs<LibreApiResponse.Error>(response)
        assertEquals(401, error.status)
        assertEquals("Unauthorized", error.error?.message)
    }
}
