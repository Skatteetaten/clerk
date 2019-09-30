package no.skatteetaten.aurora.clerk.service

import mu.KotlinLogging
import no.skatteetaten.aurora.clerk.controller.ApplicationController
import no.skatteetaten.aurora.clerk.controller.ScalePayload
import no.skatteetaten.aurora.clerk.controller.ScaleResult
import no.skatteetaten.aurora.openshift.webclient.OpenShiftClient
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class DeploymentConfigService(
    val client: OpenShiftClient,
    val podService: PodService
) {

    fun scale(
        body: ScalePayload,
        namespace: String,
        sleep: Long
    ): List<ScaleResult> {
        logger.debug("scaling all pods")
        val scaled = body.apps.associateWith { client.serviceAccount().scale(namespace, it.name, it.replicas).block() }

        logger.debug("sleeping=${sleep}ms")
        Thread.sleep(sleep)
        val scaleResult = scaled.map {
            val pods = podService.getPodItems(namespace, it.key.name)
            ScaleResult(it.key, it.value, pods)
        }
        logger.debug("found pods")
        return scaleResult
    }
}