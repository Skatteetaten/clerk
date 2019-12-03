package no.skatteetaten.aurora.clerk.service

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.fkorotkov.kubernetes.metadata
import com.fkorotkov.kubernetes.newPod
import com.fkorotkov.kubernetes.newPodList
import com.fkorotkov.kubernetes.status
import io.fabric8.kubernetes.api.model.Pod
import no.skatteetaten.aurora.clerk.controller.PodItem
import no.skatteetaten.aurora.mockmvc.extensions.mockwebserver.execute
import org.junit.jupiter.api.Test
import java.time.Instant

class PodServiceTest : AbstractOpenShiftServerTest() {

    val namespace = "jedi-test"
    val podService = PodService(openShiftClient)

    @Test
    fun `fetch a single pod`() {

        withPods(createPod("yoda")) {
            assertThat(it.count()).isEqualTo(1)
            val pod = it.first()
            assertThat(pod.name).isEqualTo("yoda-1")
            assertThat(pod.applicationName).isEqualTo("yoda")
            assertThat(pod.status).isEqualTo("Running")
            assertThat(pod.startTime).isEqualTo(Instant.EPOCH.toString())
        }
    }

    @Test
    fun `should ignore build pods when fetching pods`() {
        withPods(createPod("yoda", podLabel = mapOf(buildPodLabel to "foo"))) {
            assertThat(it).isEmpty()
        }
    }

    @Test
    fun `should ignore deploy pods when fetching pods`() {
        withPods(createPod("yoda", podLabel = mapOf(deployPodLabel to "foo"))) {
            assertThat(it).isEmpty()
        }
    }

    fun withPods(vararg pod: Pod, name: String? = null, fn: (List<PodItem>) -> Unit) {
        val podList = newPodList {
            items = pod.toList()
        }
        openShiftMock().execute(podList) {
            val result = podService.getPodItems(namespace, name)
            fn(result)
        }
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
