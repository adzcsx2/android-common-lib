package com.hoyn.common.network

import com.blankj.utilcode.util.JsonUtils
import okhttp3.Request
import okhttp3.internal.platform.Platform
import okhttp3.logging.HttpLoggingInterceptor.Level
import okio.Buffer
import java.io.IOException

/**
 * 借鉴其他Demo里的日志打印 类
 * 当日志比较多时，有时候会出现输出不全的情况
 */
object Printer {
    /** 换行符 */
    private val LINE_SEPARATOR = System.getProperty("line.separator") ?: "\n"

    /** 双换行符 */
    private val DOUBLE_SEPARATOR = LINE_SEPARATOR + LINE_SEPARATOR

    /** 省略响应体的提示信息 */
    private val OMITTED_RESPONSE = arrayOf(LINE_SEPARATOR, "Omitted response body")

    /** 省略请求体的提示信息 */
    private val OMITTED_REQUEST = arrayOf(LINE_SEPARATOR, "Omitted request body")

    private const val N = "\n"
    private const val T = "\t"

    /** 请求日志上边框 */
    private const val REQUEST_UP_LINE =
        "┌────── Request ────────────────────────────────────────────────────────────────────────"

    /** 日志结束下边框 */
    private const val END_LINE =
        "└───────────────────────────────────────────────────────────────────────────────────────"

    /** 响应日志上边框 */
    private const val RESPONSE_UP_LINE =
        "┌────── Response ───────────────────────────────────────────────────────────────────────"

    /** 请求体/响应体标签 */
    private const val BODY_TAG = "Body:"

    /** URL 标签 */
    private const val URL_TAG = "URL: "

    /** 请求方法标签 */
    private const val METHOD_TAG = "Method: @"

    /** 请求头标签 */
    private const val HEADERS_TAG = "Headers:"

    /** 状态码标签 */
    private const val STATUS_CODE_TAG = "Status Code: "

    /** 耗时标签 */
    private const val RECEIVED_TAG = "Received in: "

    /** 上边角符号 */
    private const val CORNER_UP = "┌ "

    /** 下边角符号 */
    private const val CORNER_BOTTOM = "└ "

    /** 中间连接符号 */
    private const val CENTER_LINE = "├ "

    /** 默认行前缀符号 */
    private const val DEFAULT_LINE = "│ "

    /**
     * 判断字符串是否为空
     *
     * 空字符串、换行符、制表符或纯空白字符均视为空
     *
     * @param line 待检测字符串
     * @return true 表示为空
     */
    private fun isEmpty(line: String) =
        line.isEmpty() || N == line || T == line || line.trim().isEmpty()

    /**
     * 打印 JSON 类型的请求日志
     *
     * 输出请求的 URL、方法、Content-Type、请求头和请求体
     *
     * @param builder 日志拦截器实例
     * @param request 请求对象
     */
    internal fun printJsonRequest(builder: LoggingInterceptor, request: Request) {
        val requestBody = LINE_SEPARATOR + BODY_TAG + LINE_SEPARATOR + bodyToString(request)
        val tag = builder.requestTag
        if (builder.logger == null)
            log(builder.type, tag, REQUEST_UP_LINE)
        logLines(builder.type, tag, arrayOf(URL_TAG + request.url), builder.logger, false)
        logLines(builder.type, tag, getRequest(request, builder.level), builder.logger, true)
        if (builder.level == Level.BASIC || builder.level == Level.BODY) {
            logLines(
                builder.type,
                tag,
                requestBody.split(LINE_SEPARATOR.toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray(),
                builder.logger,
                true
            )
        }
        if (builder.logger == null)
            log(builder.type, tag, END_LINE)
    }

    /**
     * 打印 JSON 类型的响应日志
     *
     * 输出响应的 URL 路径、成功状态、耗时、状态码、响应头和格式化后的 JSON 响应体
     *
     * @param builder 日志拦截器实例
     * @param chainMs 请求耗时（毫秒）
     * @param isSuccessful 是否成功
     * @param code HTTP 状态码
     * @param headers 响应头字符串
     * @param bodyString 响应体字符串
     * @param segments URL 路径分段列表
     */
    internal fun printJsonResponse(
        builder: LoggingInterceptor, chainMs: Long, isSuccessful: Boolean,
        code: Int, headers: String, bodyString: String, segments: List<String>
    ) {
        val responseBody =
            LINE_SEPARATOR + BODY_TAG + LINE_SEPARATOR + JsonUtils.formatJson(bodyString)
        val tag = builder.responseTag
        if (builder.logger == null)
            log(builder.type, tag, RESPONSE_UP_LINE)

        logLines(
            builder.type, tag, getResponse(
                headers, chainMs, code, isSuccessful,
                builder.level, segments
            ), builder.logger, true
        )
        if (builder.level == Level.BASIC || builder.level == Level.BODY) {
            logLines(
                builder.type,
                tag,
                responseBody.split(LINE_SEPARATOR.toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray(),
                builder.logger,
                true
            )
        }
        if (builder.logger == null)
            log(builder.type, tag, END_LINE)
    }

    /**
     * 打印文件类型的请求日志
     *
     * 输出请求的 URL、方法、Content-Type、请求头
     * 请求体有内容时打印请求体，无内容时打印省略提示
     *
     * @param builder 日志拦截器实例
     * @param request 请求对象
     */
    internal fun printFileRequest(builder: LoggingInterceptor, request: Request) {
        val tag = builder.responseTag
        if (builder.logger == null)
            log(builder.type, tag, REQUEST_UP_LINE)
        logLines(builder.type, tag, arrayOf(URL_TAG + request.url), builder.logger, false)
        logLines(builder.type, tag, getRequest(request, builder.level), builder.logger, true)
        if (builder.level == Level.BASIC || builder.level == Level.BODY) {
            val body = bodyToString(request)
            if (body.isNotEmpty()) {
                val requestBody = LINE_SEPARATOR + BODY_TAG + LINE_SEPARATOR + body
                logLines(
                    builder.type,
                    tag,
                    requestBody.split(LINE_SEPARATOR.toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray(),
                    builder.logger,
                    true
                )
            } else {
                logLines(builder.type, tag, OMITTED_REQUEST, builder.logger, true)
            }
        }
        if (builder.logger == null)
            log(builder.type, tag, END_LINE)
    }

    /**
     * 打印文件类型的响应日志
     *
     * 输出响应的 URL 路径、成功状态、耗时、状态码、响应头
     * 响应体以 "Omitted response body" 提示省略
     *
     * @param builder 日志拦截器实例
     * @param chainMs 请求耗时（毫秒）
     * @param isSuccessful 是否成功
     * @param code HTTP 状态码
     * @param headers 响应头字符串
     * @param segments URL 路径分段列表
     */
    internal fun printFileResponse(
        builder: LoggingInterceptor, chainMs: Long, isSuccessful: Boolean,
        code: Int, headers: String, segments: List<String>
    ) {
        val tag = builder.responseTag
        if (builder.logger == null)
            log(builder.type, tag, RESPONSE_UP_LINE)

        logLines(
            builder.type, tag, getResponse(
                headers, chainMs, code, isSuccessful,
                builder.level, segments
            ), builder.logger, true
        )
        logLines(builder.type, tag, OMITTED_RESPONSE, builder.logger, true)
        if (builder.logger == null)
            log(builder.type, tag, END_LINE)
    }

    /**
     * 构建请求日志信息
     *
     * 包含请求方法、Content-Type，并根据日志级别决定是否包含请求头
     *
     * @param request 请求对象
     * @param level 日志记录级别
     * @return 请求日志行数组
     */
    private fun getRequest(request: Request, level: Level): Array<String> {
        val message: String
        val header = request.headers.toString()
        val loggableHeader = level == Level.HEADERS || level == Level.BASIC
        val contentType = request.body?.contentType()?.toString() ?: "none"
        message = METHOD_TAG + request.method + LINE_SEPARATOR +
                "Content-Type: $contentType" + DOUBLE_SEPARATOR +
                when {
                    loggableHeader -> "${HEADERS_TAG}${LINE_SEPARATOR}${dotHeaders(header)}"
                    else -> ""
                }
        return message.split(LINE_SEPARATOR.toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
    }

    /**
     * 构建响应日志信息
     *
     * 包含 URL 路径、成功状态、耗时、状态码，并根据日志级别决定是否包含响应头
     *
     * @param header 响应头字符串
     * @param tookMs 请求耗时（毫秒）
     * @param code HTTP 状态码
     * @param isSuccessful 是否成功
     * @param level 日志记录级别
     * @param segments URL 路径分段列表
     * @return 响应日志行数组
     */
    private fun getResponse(
        header: String, tookMs: Long, code: Int, isSuccessful: Boolean,
        level: Level, segments: List<String>
    ): Array<String> {
        val message: String
        val loggableHeader = level == Level.HEADERS || level == Level.BASIC
        val segmentString = slashSegments(segments)
        message =
            "${if (segmentString.isNotEmpty()) "$segmentString - " else ""}is success : $isSuccessful - $RECEIVED_TAG$tookMs ms $DOUBLE_SEPARATOR $STATUS_CODE_TAG " +
                    "$code $DOUBLE_SEPARATOR ${
                        if (loggableHeader) HEADERS_TAG + LINE_SEPARATOR + dotHeaders(
                            header
                        ) else ""
                    }"
        return message.split(LINE_SEPARATOR.toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
    }

    /**
     * 将 URL 路径分段列表拼接为带斜杠前缀的路径字符串
     *
     * @param segments URL 路径分段列表
     * @return 拼接后的路径字符串，如 "/segment1/segment2"
     */
    private fun slashSegments(segments: List<String>): String {
        val segmentString = StringBuilder()
        for (segment in segments) {
            segmentString.append("/").append(segment)
        }
        return segmentString.toString()
    }

    /**
     * 格式化响应头字符串
     *
     * 为多行响应头添加树形前缀符号（上边角、中间连接、下边角）
     *
     * @param header 原始响应头字符串
     * @return 格式化后的响应头字符串
     */
    private fun dotHeaders(header: String): String {
        if (isEmpty(header)) return ""
        val headers =
            header.split(LINE_SEPARATOR.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val builder = StringBuilder()
        var tag = "─ "
        if (headers.size > 1) {
            for (i in headers.indices) {
                tag = when (i) {
                    0 -> CORNER_UP
                    headers.size - 1 -> CORNER_BOTTOM
                    else -> CENTER_LINE
                }
                builder.append(tag).append(headers[i]).append("\n")
            }
        } else {
            for (item in headers) {
                builder.append(tag).append(item).append("\n")
            }
        }
        return builder.toString()
    }

    /**
     * 逐行输出日志
     *
     * 将每行日志按最大宽度截断输出，超出部分自动换行
     *
     * @param type 日志级别
     * @param tag 日志标签
     * @param lines 日志行数组
     * @param logger 自定义日志输出器，为 null 时使用默认输出并添加行前缀
     * @param withLineSize 是否限制每行最大宽度（110 字符）
     */
    private fun logLines(
        type: Int,
        tag: String,
        lines: Array<String>,
        logger: LoggingInterceptor.Logger?,
        withLineSize: Boolean
    ) {
        for (line in lines) {
            val lineLength = line.length
            val maxSize = if (withLineSize) 110 else lineLength
            for (i in 0..lineLength / maxSize) {
                val start = i * maxSize
                var end = (i + 1) * maxSize
                end = if (end > line.length) line.length else end
                if (logger == null) {
                    log(type, tag, DEFAULT_LINE + line.substring(start, end))
                } else {
                    logger.log(type, tag, line.substring(start, end))
                }
            }
        }
    }

    /**
     * 将请求体转换为格式化后的 JSON 字符串
     *
     * 通过复制请求对象并写入 Buffer 来读取请求体内容，
     * 避免消费原始请求体导致后续无法再次读取
     *
     * @param request 请求对象
     * @return 格式化后的请求体字符串，读取失败时返回错误 JSON
     */
    private fun bodyToString(request: Request): String {
        try {
            val copy = request.newBuilder().build()
            val buffer = Buffer()
            if (copy.body == null)
                return ""
            copy.body!!.writeTo(buffer)
            return JsonUtils.formatJson(buffer.readUtf8())
        } catch (e: IOException) {
            return "{\"err\": \"${e.message}\"}"
        }

    }

    /**
     * 输出单条日志
     *
     * @param type 日志级别
     * @param tag 日志标签
     * @param msg 日志内容
     */
    private fun log(type: Int, tag: String, msg: String) {
        Platform.get().log(msg, type, null)
    }
}
