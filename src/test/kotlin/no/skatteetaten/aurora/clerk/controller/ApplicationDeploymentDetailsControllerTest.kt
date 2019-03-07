package no.skatteetaten.aurora.clerk.controller

import no.skatteetaten.aurora.clerk.controller.security.BearerAuthenticationManager
import no.skatteetaten.aurora.clerk.service.PodService
import no.skatteetaten.aurora.clerk.service.openshift.token.UserDetailsProvider
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant

@WebMvcTest(
    ApplicationController::class,
    PodResourceAssembler::class,
    UserDetailsProvider::class,
    BearerAuthenticationManager::class
)
@AutoConfigureWebClient
@AutoConfigureRestDocs
class ApplicationDeploymentDetailsControllerTest : AbstractSecurityControllerTest() {

    @MockBean
    lateinit var podService: PodService

    val name = "luke"
    val started = Instant.now().toString()
    val namespace = "jedi-test"

    val luke = PodItem("$name-1", name, started, "Running")

    val yoda = PodItem("yoda-1", "yoda", started, "Running")

    @Test
    @WithUserDetails
    fun `should get error if incorrect namespace in token`() {
        mockMvc.get(
            HttpHeaders().authorization("Bearer <token>"),
            "error-pods",
            "/api/pods/{namespace}", "sith"
        ) {
            it.status(HttpStatus.UNAUTHORIZED)
                .jsonPathEquals("$.success", false)
                .jsonPathEquals("$.message", "Only an application in the same namespace can use clerk.")
        }
    }

    @Test
    @WithUserDetails
    fun `should get all pods in namespace`() {

        given(podService.getPodItems(namespace)).willReturn(listOf(luke, yoda))

        mockMvc.perform(
            get("/api/pods/{namespace}", namespace)
                .header("Authorization", "Bearer <token>")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.items[0].name", `is`("luke-1")))
            .andExpect(jsonPath("$.items[0].applicationName", `is`("luke")))
            .andExpect(jsonPath("$.items[0].startTime", `is`(started)))
            .andExpect(jsonPath("$.items[0].status", `is`("Running")))
            .andExpect(jsonPath("$.items[1].name", `is`("yoda-1")))
            .andExpect(jsonPath("$.items[1].applicationName", `is`("yoda")))
            .andExpect(jsonPath("$.items[1].startTime", `is`(started)))
            .andExpect(jsonPath("$.items[1].status", `is`("Running")))
            .andDo(document("list-pods"))
    }

    @Test
    @WithUserDetails
    fun `should get pods for application in namespace`() {

        given(podService.getPodItems(namespace, name)).willReturn(listOf(luke))

        mockMvc.perform(
            get("/api/pods/{namespace}?applicationName=$name", namespace)
                .header("Authorization", "Bearer <token>")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.items[0].name", `is`("luke-1")))
            .andExpect(jsonPath("$.items[0].applicationName", `is`("luke")))
            .andExpect(jsonPath("$.items[0].startTime", `is`(started)))
            .andExpect(jsonPath("$.items[0].status", `is`("Running")))
            .andDo(document("app-pods"))
    }
}
