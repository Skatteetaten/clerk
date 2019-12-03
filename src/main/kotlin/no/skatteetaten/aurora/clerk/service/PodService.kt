package no.skatteetaten.aurora.clerk.service

import mu.KotlinLogging
import no.skatteetaten.aurora.clerk.controller.PodItem
import no.skatteetaten.aurora.openshift.webclient.OpenShiftClient
import no.skatteetaten.aurora.openshift.webclient.retryWithLog
import org.springframework.stereotype.Service

val deployPodLabel = "openshift.io/deployer-pod-for.name"
val buildPodLabel = "openshift.io/build.name"

private val logger = KotlinLogging.logger {}

@Service
class PodService(val client: OpenShiftClient) {

    fun getPodItems(
        namespace: String,
        applicationName: String? = null
    ): List<PodItem> {

        val appLabels = applicationName?.let {
            mapOf("app" to it)
        } ?: emptyMap()

        return client.serviceAccount().pods(namespace, labelMap = appLabels)
            .retryWithLog(retryFirstInMs = 100L, retryMaxInMs = 2000L)
            .map { podList ->
                podList.items.filter {
                    val labels = it.metadata.labels
                    !labels.containsKey(deployPodLabel) && !labels.containsKey(buildPodLabel)
                }.map {
                    PodItem(
                        name = it.metadata.name,
                        applicationName = it.metadata.labels["name"] ?: it.metadata.labels["app"],
                        startTime = it.status.startTime,
                        status = it.status.phase
                    )
                }
            }.block() ?: emptyList()
    }
}
