package no.skatteetaten.aurora.clerk.service

import io.fabric8.openshift.client.OpenShiftClient
import no.skatteetaten.aurora.clerk.TargetToken
import no.skatteetaten.aurora.clerk.TokenTypes
import no.skatteetaten.aurora.clerk.controller.PodItem
import org.springframework.stereotype.Service

@Service
class PodService(@TargetToken(TokenTypes.CLERK) val client: OpenShiftClient) {

    fun getPodItems(
        namespace: String,
        applicationName: String?
    ): List<PodItem> {
        val request = client.pods().inNamespace(namespace)
        val pods = applicationName?.let {
            request.withLabel("name", applicationName).list()
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