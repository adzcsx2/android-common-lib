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
        /**
         * 检查客户端证书是否可信
         *
         * 此实现信任所有客户端证书
         *
         * @param chain 客户端证书链
         * @param authType 认证类型
         * @throws CertificateException 证书异常
         */
        @SuppressLint("TrustAllX509TrustManager")
        @Throws(CertificateException::class)
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        /**
         * 检查服务端证书是否可信
         *
         * 此实现信任所有服务端证书
         *
         * @param chain 服务端证书链
         * @param authType 认证类型
         * @throws CertificateException 证书异常
         */
        @SuppressLint("TrustAllX509TrustManager")
        @Throws(CertificateException::class)
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        /**
         * 获取接受的证书颁发者列表
         *
         * @return 空的证书颁发者数组
         */
        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return arrayOf()
        }
    }

    /**
     * 创建 SSL Socket Factory
     *
     * 创建一个信任所有证书的 SSL Socket Factory，用于 HTTPS 请求
     * 注意：此方法仅用于开发环境，生产环境请使用正式证书
     *
     * @return SSL Socket Factory 实例，创建失败返回 null
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
     *
     * 获取一个信任所有主机名的 HostnameVerifier 实例
     * 注意：此方法仅用于开发环境，生产环境请使用正式的 Hostname 验证
     *
     * @return HostnameVerifier 实例
     */
    val hostnameVerifier: HostnameVerifier = HostnameVerifier { _: String?, _: SSLSession? -> true }
}
