package com.hoyn.common.core.gson

import com.google.gson.stream.JsonToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class GsonUtilsTest {

    @Test
    fun `getGson returns singleton instance`() {
        GsonUtils.init()

        val first = GsonUtils.getGson()
        val second = GsonUtils.getGson()

        assertSame(first, second)
    }

    @Test
    fun `init forwards gson parse exception events`() {
        val events = mutableListOf<GsonParseExceptionEvent>()
        GsonUtils.init { event ->
            events += event
        }

        val result = GsonUtils.fromJson<NamePayload>("""{"name":{}}""")

        assertEquals("", result?.name)
        assertEquals(1, events.size)
        assertEquals(GsonParseExceptionKind.OBJECT, events.first().kind)
        assertEquals("name", events.first().fieldName)
        assertEquals(JsonToken.BEGIN_OBJECT, events.first().jsonToken)
    }

    @Test
    fun `toJson keeps integer like doubles without decimal point`() {
        GsonUtils.init()

        val json = GsonUtils.toJson(NumberPayload(1.0))

        assertTrue(json.contains("\"value\":1"))
    }

    @Test
    fun `fromJson supports list generic type`() {
        GsonUtils.init()

        val json = """[{"name":"Alice"},{"name":"Bob"}]"""
        val result = GsonUtils.fromJson<List<NamePayload>>(json)

        assertEquals(2, result?.size)
        assertEquals("Alice", result?.get(0)?.name)
        assertEquals("Bob", result?.get(1)?.name)
    }

    private data class NamePayload(
        val name: String = ""
    )

    private data class NumberPayload(
        val value: Double
    )
}
