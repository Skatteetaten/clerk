package no.skatteetaten.aurora.openshift.webclient

import java.io.FileInputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.TrustManagerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.util.StreamUtils
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import io.netty.channel.ChannelOption
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import mu.KotlinLogging
import no.skatteetaten.aurora.webflux.AuroraWebClientCustomizer
import no.skatteetaten.aurora.webflux.config.WebFluxStarterApplicationConfig
import reactor.netty.http.client.HttpClient
import reactor.netty.tcp.SslProvider
import reactor.netty.tcp.TcpClient

private val logger = KotlinLogging.logger {}

@Configuration
@EnableAutoConfiguration(exclude = [WebFluxStarterApplicationConfig::class])
class OpenShiftClientConfig(@Value("\${spring.application.name}") val applicationName: String) {

    @Bean
    fun openShiftClient(@Qualifier("openshift") webClient: WebClient) = OpenShiftClient(webClient)

    @Qualifier("openshift")
    @Bean
    fun webClient(
        builder: WebClient.Builder,
        @Qualifier("openshift") tcpClient: TcpClient,
        @Value("\${integrations.openshift.url}") openshiftUrl: String,
        @Value("\${integrations.openshift.tokenLocation:file:/var/run/secrets/kubernetes.io/serviceaccount/token}") token: Resource
    ): WebClient {
        logger.debug("OpenshiftUrl=$openshiftUrl")
        val b = builder
            .baseUrl(openshiftUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .clientConnector(ReactorClientHttpConnector(HttpClient.from(tcpClient).compress(true)))

        try {
            b.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer ${token.readContent()}")
        } catch (e: IOException) {
            logger.info("No token file found, will not add Authorization header to WebClient")
        }

        return b.build()
    }

    @Qualifier("openshift")
    @Bean
    fun tcpClient(
        @Value("\${integrations.openshift.readTimeout:5000}") readTimeout: Long,
        @Value("\${integrations.openshift.writeTimeout:5000}") writeTimeout: Long,
        @Value("\${integrations.openshift.connectTimeout:5000}") connectTimeout: Int,
        @Qualifier("openshift") trustStore: KeyStore?
    ): TcpClient {
        val trustFactory = TrustManagerFactory.getInstance("X509")
        trustFactory.init(trustStore)

        val sslProvider = SslProvider.builder().sslContext(
            SslContextBuilder
                .forClient()
                .trustManager(trustFactory)
                .build()
        ).build()
        return TcpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
            .secure(sslProvider)
            .doOnConnected { connection ->
                connection
                    .addHandlerLast(ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
                    .addHandlerLast(WriteTimeoutHandler(writeTimeout, TimeUnit.MILLISECONDS))
            }
    }

    @Bean
    @Qualifier("openshift")
    @Profile("local")
    fun localKeyStore(): KeyStore? = null

    @Bean
    fun webClientCustomizer(): WebClientCustomizer? = AuroraWebClientCustomizer(applicationName)

    @Profile("openshift")
    @Bean
    @Qualifier("openshift")
    fun openshiftSSLContext(@Value("\${trust.store}") trustStoreLocation: String): KeyStore? =
        KeyStore.getInstance(KeyStore.getDefaultType())?.let { ks ->
            ks.load(FileInputStream(trustStoreLocation), "changeit".toCharArray())
            val fis = FileInputStream("/var/run/secrets/kubernetes.io/serviceaccount/ca.crt")
            CertificateFactory.getInstance("X509").generateCertificates(fis).forEach {
                ks.setCertificateEntry((it as X509Certificate).subjectX500Principal.name, it)
            }
            ks
        }
}

fun Resource.readContent() = StreamUtils.copyToString(this.inputStream, StandardCharsets.UTF_8)
