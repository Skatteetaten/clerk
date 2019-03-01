package no.skatteetaten.aurora.clerk.controller

import no.skatteetaten.aurora.clerk.AbstractSecurityControllerTest
import no.skatteetaten.aurora.clerk.controller.security.BearerAuthenticationManager
import no.skatteetaten.aurora.clerk.service.PodService
import no.skatteetaten.aurora.clerk.service.openshift.token.UserDetailsProvider
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
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
class ApplicationDeploymentDetailsControllerTest : AbstractSecurityControllerTest() {

    @MockBean
    lateinit var podService: PodService

    val namespace = "jedi-test"
    val pods = listOf(PodItem("luke-1", "luke", Instant.now().toString(), "Running"))

    @Test
    @WithUserDetails
    fun `should get all pods in namespace`() {

        given(podService.getPodItems(namespace)).willReturn(pods)

        mockMvc.perform(get("/api/pods/{namespace}", namespace))
            .andExpect(status().isOk)
            .andExpect(
                jsonPath(
                    "$.items[0].name",
                    `is`("luke-1")
                )
            )
    }
}