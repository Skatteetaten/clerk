package no.skatteetaten.aurora.clerk.service

import mu.KotlinLogging
import no.skatteetaten.aurora.clerk.controller.DeletePodAndScaleResult
import no.skatteetaten.aurora.clerk.controller.NoSuchResourceException
import no.skatteetaten.aurora.clerk.controller.ScaleCommand
import no.skatteetaten.aurora.clerk.controller.ScaleResult
import no.skatteetaten.aurora.openshift.webclient.OpenShiftClient
import no.skatteetaten.aurora.openshift.webclient.getDeploymentConfigName
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class DeploymentConfigService(
    val client: OpenShiftClient,
    val podService: PodService,
    @Value("\${gobo.wait.afterDeletePodTime}") val waitAfterDeletePodTime: Long = 500
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

    fun deletePodAndScaleDown(namespace: String, name: String): DeletePodAndScaleResult {
        val pod = client.serviceAccount().pod(namespace, name).block()
            ?: throw NoSuchResourceException("Cannot find Pod $name in $namespace")

        val dcName = pod.getDeploymentConfigName()
            ?: throw RuntimeException("Pod $name does'nt have a DeploymentConfig annotation")

        val dc = client.serviceAccount().deploymentConfig(namespace, dcName).block()
            ?: throw NoSuchResourceException("Cannot find DeploymentConfig $dcName in $namespace")

        logger.info("Deleting pod={} in namespace={}", name, namespace)
        client.serviceAccount().deletePod(namespace, name).block()

        Thread.sleep(waitAfterDeletePodTime)

        val replicas = dc.spec.replicas - 1
        logger.info("Scaling dc={} in namespace={} from {} replica(s) to {} replica(s)", dc.metadata.name, dc.metadata.namespace, dc.spec.replicas, replicas)
        val result = client.serviceAccount().scale(namespace, dcName, replicas).block()

        return DeletePodAndScaleResult(currentReplicas = replicas, deletedPodName = name, scaleResult = result)
    }
}
