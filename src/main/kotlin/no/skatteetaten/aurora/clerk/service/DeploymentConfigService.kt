package no.skatteetaten.aurora.clerk.service

import mu.KotlinLogging
import no.skatteetaten.aurora.clerk.controller.NoSuchResourceException
import no.skatteetaten.aurora.clerk.controller.ScaleCommand
import no.skatteetaten.aurora.clerk.controller.ScaleResult
import no.skatteetaten.aurora.openshift.webclient.OpenShiftClient
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClientException

private val logger = KotlinLogging.logger {}

@Service
class DeploymentConfigService(
    val client: OpenShiftClient,
    val podService: PodService
) {

    fun scale(
        command: ScaleCommand,
        namespace: String,
        sleep: Long
    ): ScaleResult {

        logger.debug("scaling all pods")
        val result = client.serviceAccount().scale(namespace, command.name, command.replicas).block()

        logger.debug("sleeping=${sleep}ms")
        Thread.sleep(sleep)

        logger.debug("fetching all pods")
        val pods = podService.getPodItems(namespace, command.name)

        return ScaleResult(command, result, pods)
    }
}