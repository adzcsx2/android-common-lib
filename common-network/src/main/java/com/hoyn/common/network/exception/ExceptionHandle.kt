package com.hoyn.common.network.exception

import android.net.ParseException
import com.google.gson.JsonParseException
import com.google.gson.stream.MalformedJsonException
import org.json.JSONException
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * 网络异常处理工具类
 */
object ExceptionHandle {

    /**
     * 处理网络异常
     */
    fun handleException(e: Throwable): ResponseThrowable {
        return when (e) {
            is HttpException -> ResponseThrowable(ERROR.HTTP_ERROR, e)
            is SocketTimeoutException -> ResponseThrowable(ERROR.TIMEOUT_ERROR, e)
            is ConnectException -> ResponseThrowable(ERROR.NETWORK_ERROR, e)
            is UnknownHostException -> ResponseThrowable(ERROR.NETWORK_ERROR, e)
            is SSLException -> ResponseThrowable(ERROR.SSL_ERROR, e)
            is JsonParseException -> ResponseThrowable(ERROR.PARSE_ERROR, e)
            is MalformedJsonException -> ResponseThrowable(ERROR.PARSE_ERROR, e)
            is JSONException -> ResponseThrowable(ERROR.PARSE_ERROR, e)
            is ParseException -> ResponseThrowable(ERROR.PARSE_ERROR, e)
            is ResponseThrowable -> e
            else -> ResponseThrowable(ERROR.UNKNOWN, e)
        }
    }
}
