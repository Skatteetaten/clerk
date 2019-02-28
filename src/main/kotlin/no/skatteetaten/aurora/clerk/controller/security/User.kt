package no.skatteetaten.aurora.clerk.controller.security

import org.springframework.security.core.GrantedAuthority
import kotlin.math.min
import org.springframework.security.core.userdetails.User as SpringSecurityUser

class User(
    username: String,
    val token: String,
    grantedAuthorities: Collection<GrantedAuthority> = listOf()
) : SpringSecurityUser(username, token, true, true, true, true, grantedAuthorities.toList()) {
    val tokenSnippet: String
        get() = token.substring(0, min(token.length, 5))
}