package no.skatteetaten.aurora.clerk.controller

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.containing
import no.skatteetaten.aurora.clerk.AbstractTest
import no.skatteetaten.aurora.clerk.TestUserDetailsService
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.contract.wiremock.restdocs.ContractResultHandler
import org.springframework.cloud.contract.wiremock.restdocs.WireMockRestDocs
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.web.util.UriComponentsBuilder

@ExtendWith(SpringExtension::class, RestDocumentationExtension::class)
@Import(TestUserDetailsService::class)
open class AbstractSecurityControllerTest : AbstractTest() {

    @Autowired
    lateinit var mockMvc: MockMvc
}

fun HttpHeaders.authorization(value: String): HttpHeaders {
    this.set(HttpHeaders.AUTHORIZATION, value)
    return this
}

data class MockMvcData(val requestUrl: String, val results: ResultActions) : ResultActions by results {
    fun get() = WireMock.get(requestUrl)!!
}

fun MockMvc.getWithDocsBeaer(
    docs: String, urlTemplate: String, vararg uriVars: String,
    fn: (mockMvcData: MockMvcData) -> Unit
) {
    val headers = HttpHeaders().authorization("Bearer <token>")
    val url = UriComponentsBuilder.fromUriString(urlTemplate).buildAndExpand(*uriVars).encode().toUri()
    val builder = MockMvcRequestBuilders.get(urlTemplate, *uriVars).headers(headers)
    val resultActions = this.perform(builder)
    val mock = MockMvcData(url.toString(), resultActions)
    fn(mock)
    mock.andDo(verifyWireMock(mock.get().withHeader(AUTHORIZATION, containing("Bearer"))))
        .andDo(document(docs))
}

fun MockMvc.get(
    headers: HttpHeaders,
    urlTemplate: String,
    vararg uriVars: String,
    fn: (mockMvcData: MockMvcData) -> Unit
) {
    val url = UriComponentsBuilder.fromUriString(urlTemplate).buildAndExpand(*uriVars).encode().toUri()
    val builder = MockMvcRequestBuilders.get(urlTemplate, *uriVars).headers(headers)
    val resultActions = this.perform(builder)
    fn(MockMvcData(url.toString(), resultActions))
}

fun verifyWireMock(builder: MappingBuilder) = WireMockRestDocs.verify().wiremock(builder)!!

fun ContractResultHandler.get(mockMvcData: MockMvcData): MappingBuilder? {
    val get = WireMock.get(mockMvcData.requestUrl)
    this.wiremock(get)
    return get
}
