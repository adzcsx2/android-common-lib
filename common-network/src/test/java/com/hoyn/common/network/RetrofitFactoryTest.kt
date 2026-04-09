package com.hoyn.common.network

import com.hoyn.common.core.gson.GsonUtils
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Converter

class RetrofitFactoryTest {

    @Test
    fun `create uses shared gson converter`() {
        GsonUtils.init()
        val retrofit = RetrofitFactory.create("https://retrofit-factory.example/")
        val converter = retrofit.responseBodyConverter<NamePayload>(
            NamePayload::class.java,
            emptyArray<Annotation>()
        ) as Converter<ResponseBody, NamePayload>

        val payload = converter.convert("""{"name":{}}""".toResponseBody("application/json".toMediaType()))

        assertEquals("", payload?.name)
    }

    private data class NamePayload(
        val name: String = ""
    )
}