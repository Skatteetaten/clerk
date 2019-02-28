package no.skatteetaten.aurora.clerk.service

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.fkorotkov.kubernetes.metadata
import com.fkorotkov.kubernetes.newPod
import com.fkorotkov.kubernetes.status
import io.fabric8.kubernetes.api.model.Pod
import no.skatteetaten.aurora.clerk.controller.PodItem
import org.junit.jupiter.api.Test
import java.time.Instant

class PodServiceTest : AbstractOpenShiftServerTest() {

    val namespace = "jedi-test"

    @Test
    fun `fetch a single pod`() {

        withPods(createPod("yoda")) { result ->
            assertThat(result.count()).isEqualTo(1)
            val pod = result.first()
            assertThat(pod.name).isEqualTo("yoda-1")
            assertThat(pod.applicationName).isEqualTo("yoda")
            assertThat(pod.status).isEqualTo("Running")
            assertThat(pod.startTime).isEqualTo(Instant.EPOCH.toString())
        }
    }

    @Test
    fun `should ignore build pods when fetching pods`() {
         withPods(createPod("yoda", podLabel = mapOf(buildPodLabel to "foo"))) { result ->
            assertThat(result).isEmpty()
        }
    }


    @Test
    fun `should ignore deploy pods when fetching pods`() {
         withPods(createPod("yoda", podLabel = mapOf(deployPodLabel to "foo"))) { result ->
            assertThat(result).isEmpty()
        }
    }
    fun withPods(vararg pod: Pod, fn: (List<PodItem>) -> Unit) {
        openShiftServer.openshiftClient.inNamespace(namespace).pods().create(*pod)
        val podService = PodService(openShiftServer.openshiftClient)
        val result = podService.getPodItems(namespace)
        fn(result)
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