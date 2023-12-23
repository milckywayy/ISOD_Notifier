package pl.edu.pw.ee.isod_notifier

import android.content.Context
import okhttp3.OkHttpClient
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager


fun getSslOkHttpClient(context: Context): OkHttpClient {
    val certificateFactory = CertificateFactory.getInstance("X.509")

    val inputStream: InputStream = context.resources.openRawResource(R.raw.certificate)
    val certificate = certificateFactory.generateCertificate(inputStream)
    inputStream.close()

    val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
    keyStore.load(null, null)
    keyStore.setCertificateEntry("ca", certificate)

    val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
    val trustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm)
    trustManagerFactory.init(keyStore)

    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(null, trustManagerFactory.trustManagers, null)

    return OkHttpClient.Builder()
        .sslSocketFactory(sslContext.socketFactory, trustManagerFactory.trustManagers[0] as X509TrustManager)
        .hostnameVerifier { _, _ -> true }
        .build()
}
