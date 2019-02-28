package no.skatteetaten.aurora.clerk.service

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.fkorotkov.kubernetes.metadata
import com.fkorotkov.kubernetes.newPod
import com.fkorotkov.kubernetes.status
import io.fabric8.kubernetes.api.model.Pod
import org.junit.jupiter.api.Test
import java.time.Instant

class PodServiceTest : AbstractOpenShiftServerTest() {

    val namespace = "jedi-test"

    @Test
    fun `fetch a single pod`() {

        applyPods(createPod("yoda"))

        val podService = PodService(openShiftServer.openshiftClient)

        val result = podService.getPodItems(namespace)
        assertThat(result.count()).isEqualTo(1)
        val pod = result.first()
        assertThat(pod.name).isEqualTo("yoda-1")
        assertThat(pod.applicationName).isEqualTo("yoda")
        assertThat(pod.status).isEqualTo("Running")
        assertThat(pod.startTime).isEqualTo(Instant.EPOCH.toString())
    }

    private fun applyPods(vararg pod: Pod) {
        openShiftServer.openshiftClient.inNamespace(namespace).pods().create(*pod)
    }
}

fun createPod(
    appName: String,
    instance: String = "1",
    podLabel: Map<String, String> = mapOf("app" to appName),
    podPhase: String = "Running"
): Pod {
    return newPod {
        metadata {
            name = "$appName-$instance"
            labels = podLabel
        }
        status {
            phase = podPhase
            startTime = Instant.EPOCH.toString()
        }
    }
}