package no.skatteetaten.aurora.clerk.service.openshift.token

import no.skatteetaten.aurora.clerk.controller.security.User
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class UserDetailsProvider {

    fun getAuthenticatedUser(): User {

        val authentication = SecurityContextHolder.getContext().authentication
        val user: User = authentication.principal as User
        return user
    }
}