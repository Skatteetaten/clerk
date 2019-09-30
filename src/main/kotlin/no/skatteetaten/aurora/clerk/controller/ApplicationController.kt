package no.skatteetaten.aurora.clerk.controller

import com.fasterxml.jackson.databind.JsonNode
import mu.KotlinLogging
import no.skatteetaten.aurora.clerk.service.DeploymentConfigService
import no.skatteetaten.aurora.clerk.service.PodService
import no.skatteetaten.aurora.clerk.service.openshift.token.UserDetailsProvider
import no.skatteetaten.aurora.openshift.webclient.OpenShiftClient
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api")
class ApplicationController(
    val userProvider: UserDetailsProvider,
    val deploymentConfigService: DeploymentConfigService,
    val podService: PodService,
    val client: OpenShiftClient
) {

    /* killPods
        hent alle pods, sjekk av killPodList er korrekt.
        finn nytt antall
        drep alle pods
        scale til nytt antall
     */

    @PutMapping("/scale/{namespace}")
    fun scale(
        @PathVariable namespace: String,
        @RequestParam("sleep", defaultValue = "500") sleep: Long,
        @RequestBody body: ScalePayload
    ): ClerkResponse<ScaleResult> {
        validateUser(namespace)

        val scaleResult = deploymentConfigService.scale(body, namespace, sleep)

        return ClerkResponse(items = scaleResult, message = "Scaled applications in namespace=${namespace}")
    }

    @GetMapping("/pods/{namespace}")
    fun findPods(
        @PathVariable namespace: String,
        @RequestParam("applicationName", required = false) applicationName: String?
    ): ClerkResponse<PodItem> {

        validateUser(namespace)
        val podItems = podService.getPodItems(namespace, applicationName)

        val namePart = applicationName?.let { "name=$applicationName" } ?: ""
        val message1 = "Fetched count=${podItems.count()} pods for namespace=$namespace $namePart"
        logger.info(message1)
        return ClerkResponse(
            items = podItems,
            message = message1
        )
    }

    private fun validateUser(namespace: String) {
        val user = userProvider.getAuthenticatedUser()
        if (!user.username.startsWith("system:serviceaccount:$namespace:")) {
            throw BadCredentialsException("Only an application in the same namespace can use clerk.")
        }
    }
}


