package no.skatteetaten.aurora.clerk.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.every
import io.mockk.mockk
import no.skatteetaten.aurora.clerk.controller.PodItem
import no.skatteetaten.aurora.clerk.controller.ScaleCommand
import no.skatteetaten.aurora.mockmvc.extensions.mockwebserver.execute
import org.junit.jupiter.api.Test
import java.time.Instant

class DeploymentConfigServiceTest : AbstractOpenShiftServerTest() {

    val namespace = "jedi-test"
    val podService: PodService = mockk()
    val dcService = DeploymentConfigService(openShiftClient, podService)

    private val name = "luke"
    private val started = Instant.now().toString()

    private val luke = PodItem("$name-1", name, started, "Running")
    val command = ScaleCommand(name = name, replicas = 1)

    @Test
    fun `scale single item`() {

        every { podService.getPodItems(namespace, name) } returns listOf(luke)
        val jsonResponse = """
            { }
        """.trimIndent()

        val response = jacksonObjectMapper().readTree(jsonResponse)

        server.execute(response) {
            val result = dcService.scale(command, namespace, 100)
            assertThat(result.pods.size).isEqualTo(1)
        }
    }
}
