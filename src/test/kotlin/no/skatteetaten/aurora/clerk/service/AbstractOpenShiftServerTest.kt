package no.skatteetaten.aurora.clerk.service

import no.skatteetaten.aurora.openshift.webclient.OpenShiftClient
import okhttp3.mockwebserver.MockWebServer
import org.springframework.web.reactive.function.client.WebClient

open class AbstractOpenShiftServerTest {

    val server = MockWebServer()
    val url = server.url("/")

    val webClient = WebClient.create(url.toString())
    val openShiftClient = OpenShiftClient(webClient)
}
