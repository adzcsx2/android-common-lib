package com.hoyn.common.network.ssl

import android.annotation.SuppressLint
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * SSL 证书管理工具类
 */
object SSLManager {

    /**
     * 信任所有证书
     */
    class TrustAllCerts : X509TrustManager {
        @SuppressLint("TrustAllX509TrustManager")
        @Throws(CertificateException::class)
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        @SuppressLint("TrustAllX509TrustManager")
        @Throws(CertificateException::class)
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return arrayOf()
        }
    }

    /**
     * 创建 SSL Socket Factory
     */
    fun createSSLSocketFactory(): SSLSocketFactory? {
        val trustAllCerts = TrustAllCerts()
        return try {
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf<TrustManager>(trustAllCerts), null)
            sslContext.socketFactory
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            null
        } catch (e: KeyManagementException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 信任所有 Hostname
     */
    val hostnameVerifier: HostnameVerifier = HostnameVerifier { _: String?, _: SSLSession? -> true }
}
