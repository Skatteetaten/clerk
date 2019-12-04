package no.skatteetaten.aurora.clerk.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.skatteetaten.aurora.mockmvc.extensions.mockwebserver.HttpMock
import no.skatteetaten.aurora.mockmvc.extensions.mockwebserver.httpMockServer
import no.skatteetaten.aurora.openshift.webclient.OpenShiftClient
import no.skatteetaten.aurora.openshift.webclient.OpenShiftUri
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.AfterEach
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

open class AbstractOpenShiftServerTest {

    val openShiftPort = 8081
    val webClient = WebClient.create("http://localhost:$openShiftPort/")
    val openShiftClient = OpenShiftClient(webClient)

    fun openShiftMock(block: HttpMock.() -> Unit = {}): MockWebServer {
        return httpMockServer(openShiftPort, block)
    }

    @AfterEach
    fun after() {
        HttpMock.clearAllHttpMocks()
    }
}

fun RecordedRequest.matchMethodAndEndpoint(method: HttpMethod, uri: OpenShiftUri): Boolean {
    val isEndpoint = this.path?.contains(uri.expand())
    return isEndpoint !== null && isEndpoint && this.method == method.name
}

fun Any.toJsonBody(): MockResponse {
    val body = jacksonObjectMapper().writeValueAsString(this)
    return MockResponse().setBody(body).setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
}
