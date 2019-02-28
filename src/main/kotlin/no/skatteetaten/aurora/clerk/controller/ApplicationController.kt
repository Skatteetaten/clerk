package no.skatteetaten.aurora.clerk.controller

import no.skatteetaten.aurora.clerk.service.PodService
import no.skatteetaten.aurora.clerk.service.openshift.token.UserDetailsProvider
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.q3c.rest.hal.HalResource

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
    ): AuroraResponse<PodItem> {

        val user=userProvider.getAuthenticatedUser()
        val podItems = podService.getPodItems(namespace, applicationName)

        val namePart = applicationName?.let { " name=$applicationName" } ?: ""
        return podResourceAssembler.toAuroraResource(podItems, "Fetched pods for namespace=$namespace ${namePart}")
    }
}

data class PodItem(
    val name: String,
    val applicationName: String?,
    val startTime: String,
    val status: String
) : HalResource()

@Component
class PodResourceAssembler {
    fun toAuroraResource(
        podItems: List<PodItem>,
        message: String
    ): AuroraResponse<PodItem> {
        return AuroraResponse(
            items = podItems,
            message = message
        )
    }
}
