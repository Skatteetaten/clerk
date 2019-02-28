package no.skatteetaten.aurora.clerk.service

import io.fabric8.openshift.client.OpenShiftClient
import no.skatteetaten.aurora.clerk.controller.PodItem
import org.springframework.stereotype.Service

val deployPodLabel = "openshift.io/deployer-pod-for.name"
val buildPodLabel = "openshift.io/build.name"

@Service
class PodService(val client: OpenShiftClient) {

    fun getPodItems(
        namespace: String,
        applicationName: String? = null
    ): List<PodItem> {
        val request = client.pods().inNamespace(namespace)
            .withoutLabel(deployPodLabel)
            .withoutLabel(buildPodLabel)
        val pods = applicationName?.let {
            request.withLabel("app", applicationName).list()
        } ?: request.list()

        return pods.items.map {

            PodItem(
                it.metadata.name,
                it.metadata.labels["name"] ?: it.metadata.labels["app"],
                it.status.startTime,
                it.status.phase
            )
        }
    }
}