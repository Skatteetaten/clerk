package no.skatteetaten.aurora.clerk.service

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fkorotkov.kubernetes.newPodList
import io.fabric8.kubernetes.api.model.Pod
import io.mockk.every
import io.mockk.mockk
import no.skatteetaten.aurora.clerk.controller.PodItem
import no.skatteetaten.aurora.clerk.controller.ScaleCommand
import no.skatteetaten.aurora.clerk.controller.ScalePayload
import no.skatteetaten.aurora.clerk.controller.ScaleResult
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

    val scalePayload = ScalePayload(apps = listOf(command))
    @Test
    fun `scale single item`() {

        every { podService.getPodItems(namespace, name)} returns listOf(luke)
        val jsonResponse = """
            { }
        """.trimIndent()

        val response = jacksonObjectMapper().readTree(jsonResponse)

        server.execute(response) {
            val result=dcService.scale(scalePayload, namespace, 100)
            assertThat(result.size).isEqualTo(1)
            assertThat(result[0].pods.size).isEqualTo(1)
        }
    }

}

