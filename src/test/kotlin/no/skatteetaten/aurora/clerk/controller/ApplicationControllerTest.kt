package no.skatteetaten.aurora.clerk.controller

import no.skatteetaten.aurora.clerk.controller.security.BearerAuthenticationManager
import no.skatteetaten.aurora.clerk.service.DeploymentConfigService
import no.skatteetaten.aurora.clerk.service.PodService
import no.skatteetaten.aurora.clerk.service.openshift.token.UserDetailsProvider
import no.skatteetaten.aurora.mockmvc.extensions.Path
import no.skatteetaten.aurora.mockmvc.extensions.authorization
import no.skatteetaten.aurora.mockmvc.extensions.contentTypeJson
import no.skatteetaten.aurora.mockmvc.extensions.get
import no.skatteetaten.aurora.mockmvc.extensions.put
import no.skatteetaten.aurora.mockmvc.extensions.responseJsonPath
import no.skatteetaten.aurora.mockmvc.extensions.status
import no.skatteetaten.aurora.mockmvc.extensions.statusIsOk
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.security.test.context.support.WithUserDetails
import java.time.Instant

@WebMvcTest(
    ApplicationController::class,
    UserDetailsProvider::class,
    BearerAuthenticationManager::class
)
@AutoConfigureWebClient
@AutoConfigureRestDocs
class ApplicationControllerTest : AbstractSecurityControllerTest() {

    @MockBean
    lateinit var podService: PodService

    @MockBean
    lateinit var dcService: DeploymentConfigService

    private val name = "luke"
    private val started = Instant.now().toString()
    private val namespace = "jedi-test"

    private val luke = PodItem("$name-1", name, started, "Running")

    private val yoda = PodItem("yoda-1", "yoda", started, "Running")

    @Test
    @WithUserDetails
    fun `should get error if incorrect namespace in token`() {
        mockMvc.get(
            headers = HttpHeaders().authorization("Bearer <token>"),
            docsIdentifier = "error-pods",
            path = Path("/api/pods/sith")
        ) {
            status(UNAUTHORIZED)
                .responseJsonPath("$.success").equalsValue(false)
                .responseJsonPath("$.message").equalsValue("Only an application in the same namespace can use clerk.")
        }
    }

    @Test
    @WithUserDetails
    fun `should get all pods in namespace`() {
        given(podService.getPodItems(namespace)).willReturn(listOf(luke, yoda))

        mockMvc.get(
            headers = HttpHeaders().authorization("Bearer <token>"),
            path = Path("/api/pods/{namespace}", namespace)
        ) {
            statusIsOk()
                .responseJsonPath("$.items[0]").equalsObject(luke)
                .responseJsonPath("$.items[1]").equalsObject(yoda)
        }
    }

    @Test
    @WithUserDetails
    fun `should get pods for application in namespace`() {
        given(podService.getPodItems(namespace, name)).willReturn(listOf(luke))

        mockMvc.get(
            headers = HttpHeaders().authorization("Bearer <token>"),
            path = Path("/api/pods/{namespace}?applicationName=$name", namespace)
        ) {
            statusIsOk().responseJsonPath("$.items[0]").equalsObject(luke)
        }
    }

    @Test
    @WithUserDetails
    fun `should scale deployment config in namespace`() {

        val command = ScaleCommand(name = name, replicas = 1)
        val scaleResult = ScaleResult(
            command = command,
            result = null,
            pods = listOf(luke)
        )
        given(dcService.scale(command, namespace, 100)).willReturn(scaleResult)

        mockMvc.put(
            headers = HttpHeaders().authorization("Bearer <token>").contentTypeJson(),
            path = Path("/api/scale/{namespace}", namespace),
            body = command
        ) {
            statusIsOk().responseJsonPath("$.items[0].pods[0]").equalsObject(luke)
        }
    }
}
