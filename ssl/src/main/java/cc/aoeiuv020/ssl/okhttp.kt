@file:Suppress("unused")

package cc.aoeiuv020.ssl

import cc.aoeiuv020.ssl.OkhttpUtils.client
import okhttp3.*
import java.net.MalformedURLException
import java.net.URL
import java.security.cert.X509Certificate

/**
 * Created by AoEiuV020 on 2018.09.04-17:49:06.
 */


object OkhttpUtils {

    /**
     * 默认信任系统证书的OkHttpClient, 需要配置时调用baseClient.newBuilder()，
     */
    val client: OkHttpClient by lazy {
        baseClientBuilder.build()
    }
}

/**
 * 添加支持一个安卓4.x不支持的加密协议，
 * https://github.com/square/okhttp/issues/4053
 *     javax.net.ssl.SSLProtocolException
 *     : SSL handshake aborted
 *     : ssl=0xb88ec0b0
 *     : Failure in SSL library, usually a protocol error
 *     error:14077410:SSL routines
 *     :SSL23_GET_SERVER_HELLO
 *     :sslv3 alert handshake failure (external/openssl/ssl/s23_clnt.c:741 0x98947990:0x00000000)
 */
private val spec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
        .cipherSuites(
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
                *ConnectionSpec.MODERN_TLS.cipherSuites()!!.toTypedArray()
        )
        .build()

/**
 * 私有，外部不要用，不要改了这个builder, 需要时baseClient.newBuilder(),
 */
private val baseClientBuilder: OkHttpClient.Builder by lazy {
    OkHttpClient.Builder()
            // cleartext要明确指定，
            .connectionSpecs(listOf(spec, ConnectionSpec.CLEARTEXT))
            // 默认信任系统证书，
            .sslInclude()
}

/**
 * 信任所有证书，
 */
fun OkHttpClient.Builder.sslAllowAll() = apply {
    TrustManagerUtils.allowAll().let { tm ->
        sslSocketFactory((TLSSocketFactory(tm)), tm)
    }
}

/**
 * 只信任certList中包含的证书，
 */
fun OkHttpClient.Builder.sslOnly(vararg certList: X509Certificate) = apply {
    TrustManagerUtils.only(certList.toSet()).let { tm ->
        sslSocketFactory((TLSSocketFactory(tm)), tm)
    }
}

/**
 * 信任系统证书加上certList中的证书，
 */
fun OkHttpClient.Builder.sslInclude(vararg certList: X509Certificate) = apply {
    TrustManagerUtils.include(certList.toSet()).let { tm ->
        sslSocketFactory((TLSSocketFactory(tm)), tm)
    }
}

fun get(url: String): Call {
    val request = Request.Builder()
            .url(url)
            .build()
    return client.newCall(request)
}

fun Call.string(): String = this.execute().body().let { requireNotNull(it) }.use { it.string() }

fun Response.charset(): String? = body()?.contentType()?.charset()?.name()
fun Response.url(): String = this.request().url().toString()
/**
 * 地址仅路径，斜杆/开头，
 */
fun path(url: String): String = try {
    URL(url).path
} catch (e: MalformedURLException) {
    url
}