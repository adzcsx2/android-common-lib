package com.hoyn.common.network

import com.blankj.utilcode.util.JsonUtils
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.internal.platform.Platform
import okhttp3.internal.platform.Platform.Companion.INFO
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * 借鉴其他Demo里的日志打印
 * #当日志比较多时，有时候会出现输出不全的情况
 */
class LoggingInterceptor : Interceptor {

    /** 日志标签 */
    private var tag: String = "HttpLogging"

    /** 是否开启调试模式，默认跟随 NetworkConfig.isDebug */
    var isDebug: Boolean = NetworkConfig.isDebug

    /** 日志输出级别 */
    var type = INFO

    /** 请求日志标签 */
    var requestTag: String = tag

    /** 响应日志标签 */
    var responseTag: String = tag

    /** 日志记录级别 */
    var level = HttpLoggingInterceptor.Level.BASIC

    /** 请求头构建器 */
    private val headers = Headers.Builder()

    /** 自定义日志输出器，为 null 时使用默认输出器 */
    var logger: Logger? = null

    /** 默认日志输出器，使用 OkHttp 平台原生日志输出 */
    private val defaultLogger: Logger = object : Logger {
        override fun log(level: Int, tag: String, msg: String) {
            Platform.get().log(msg, level, null)
        }
    }

    /**
     * 日志输出器接口
     *
     * 用于自定义日志的输出行为，使用者可实现此接口来接管日志输出
     */
    interface Logger {
        /**
         * 输出日志
         *
         * @param level 日志级别
         * @param tag 日志标签
         * @param msg 日志内容
         */
        fun log(level: Int, tag: String, msg: String)
    }

    /**
     * 输出日志
     *
     * 优先使用自定义 Logger，未设置时使用默认日志输出器
     *
     * @param level 日志级别
     * @param tag 日志标签
     * @param msg 日志内容
     */
    internal fun log(level: Int, tag: String, msg: String) {
        (logger ?: defaultLogger).log(level, tag, msg)
    }

    /**
     * 拦截网络请求并打印日志
     *
     * 请求阶段：根据 Content-Type 决定打印 JSON 请求体还是文件请求信息
     * 响应阶段：根据 Content-Type 决定打印 JSON 响应体还是文件响应信息
     * 非调试模式或日志级别为 NONE 时直接放行
     *
     * @param chain 拦截器链
     * @return 响应结果
     * @throws IOException 网络 IO 异常
     */
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        if (getHeaders().size > 0) {
            val headers = request.headers
            val names = headers.names()
            val iterator = names.iterator()
            val requestBuilder = request.newBuilder()
            requestBuilder.headers(getHeaders())
            while (iterator.hasNext()) {
                val name = iterator.next()
                requestBuilder.addHeader(name, headers.get(name)!!)
            }
            request = requestBuilder.build()
        }

        if (!isDebug || level == HttpLoggingInterceptor.Level.NONE) {
            return chain.proceed(request)
        }
        val requestBody = request.body

        var rContentType: MediaType? = null
        if (requestBody != null) {
            rContentType = request.body!!.contentType()
        }

        var rSubtype: String? = null
        if (rContentType != null) {
            rSubtype = rContentType.subtype
        }

        if (requestBody != null && (rSubtype == null || rSubtype.contains("json") || rSubtype.contains(
                "xml"
            ) || rSubtype.contains(
                "plain"
            ) || rSubtype.contains("html") || rSubtype.contains("x-www-form-urlencoded"))
        ) {
            Printer.printJsonRequest(this, request)
        } else {
            Printer.printFileRequest(this, request)
        }

        val st = System.nanoTime()
        val response = chain.proceed(request)

        val segmentList = request.url.encodedPathSegments
        val chainMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - st)
        val header = response.headers.toString()
        val code = response.code
        val isSuccessful = response.isSuccessful
        val responseBody = response.body
        val contentType = responseBody!!.contentType()

        var subtype: String? = null
        val body: ResponseBody

        if (contentType != null) {
            subtype = contentType.subtype
        }

        if (subtype != null && (subtype.contains("json") || subtype.contains("xml") || subtype.contains(
                "plain"
            ) || subtype.contains("html"))
        ) {
            val bodyString = responseBody.string()
            val bodyJson = JsonUtils.formatJson(bodyString)
            Printer.printJsonResponse(
                this, chainMs, isSuccessful, code, header, bodyJson, segmentList
            )
            body = bodyString.toResponseBody(contentType)
        } else {
            Printer.printFileResponse(this, chainMs, isSuccessful, code, header, segmentList)
            return response
        }
        return response.newBuilder().body(body).build()
    }

    /**
     * 获取构建好的请求头
     *
     * @return 请求头 Headers 实例
     */
    private fun getHeaders(): Headers = headers.build()
}
