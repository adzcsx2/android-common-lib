package com.hoyn.common.network

import android.content.Context
import com.hoyn.common.core.gson.GsonUtils
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.Mockito.mock
import retrofit2.Converter

class HttpClientUtilsTest {

    @Test
    fun `initConfig uses shared gson converter`() {
        GsonUtils.init()
        val config = HttpClientUtils.InitRetrofitConfig().apply {
            context = mock(Context::class.java)
            baseUrl = "https://http-client-utils.example/"
        }

        HttpClientUtils.initConfig(config)
        val retrofit = HttpClientUtils.getRetrofit()
        val converter = requireNotNull(retrofit).responseBodyConverter<NamePayload>(
            NamePayload::class.java,
            emptyArray<Annotation>()
        ) as Converter<ResponseBody, NamePayload>

        val payload = converter.convert("""{"name":{}}""".toResponseBody("application/json".toMediaType()))

        assertNotNull(retrofit)
        assertEquals("", payload?.name)
    }

    private data class NamePayload(
        val name: String = ""
    )
}