package no.skatteetaten.aurora.clerk.controller.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User as SpringSecurityUser

class User(
    username: String,
    val token: String,
    grantedAuthorities: Collection<GrantedAuthority> = listOf()
) : SpringSecurityUser(username, token, true, true, true, true, grantedAuthorities.toList()) {
    val tokenSnippet: String
        get() = token.takeLast(5)
}
