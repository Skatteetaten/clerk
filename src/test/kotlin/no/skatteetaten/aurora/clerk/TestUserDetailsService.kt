package no.skatteetaten.aurora.clerk

import no.skatteetaten.aurora.clerk.controller.security.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component

@Component
class TestUserDetailsService : UserDetailsService {
    override fun loadUserByUsername(username: String?): UserDetails {
        return User("system:serviceaccount:jedi-test:default", "token")
    }
}
