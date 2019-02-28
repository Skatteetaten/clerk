package no.skatteetaten.aurora.clerk.controller

import mu.KotlinLogging
import no.skatteetaten.aurora.clerk.service.PodService
import no.skatteetaten.aurora.clerk.service.openshift.token.UserDetailsProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/")
class ApplicationController(
    val userProvider: UserDetailsProvider,
    val podService: PodService,
    val podResourceAssembler: PodResourceAssembler
) {

    @GetMapping("/pods/{namespace}")
    fun findPods(
        @PathVariable namespace: String,
        @RequestParam("applicationName", required = false) applicationName: String?
    ): ClerkResponse<PodItem> {

        validateUser(namespace)
        val podItems = podService.getPodItems(namespace, applicationName)

        val namePart = applicationName?.let { "name=$applicationName" } ?: ""
        return podResourceAssembler.toAuroraResource(podItems, "Fetched count=${podItems.count()} pods for namespace=$namespace $namePart")
    }

    private fun validateUser(namespace: String) {
        val user = userProvider.getAuthenticatedUser()
        if (!user.username.startsWith("system:serviceaccount:$namespace:")) {
            throw BadCredentialsException("Only an application in the same namespace can use clerk.")
        }
    }
}

data class PodItem(
    val name: String,
    val applicationName: String?,
    val startTime: String,
    val status: String
)

@Component
class PodResourceAssembler {
    fun <T> toAuroraResource(
        podItems: List<T>,
        message: String
    ): ClerkResponse<T> {
        logger.info(message)
        return ClerkResponse(
            items = podItems,
            message = message
        )
    }
}
