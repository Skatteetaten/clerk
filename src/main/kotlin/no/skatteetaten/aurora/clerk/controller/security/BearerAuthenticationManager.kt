package no.skatteetaten.aurora.clerk.controller.security

import mu.KotlinLogging
import no.skatteetaten.aurora.openshift.webclient.OpenShiftClient
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.stereotype.Component
import java.util.regex.Pattern

private val logger = KotlinLogging.logger {}

@Component
class BearerAuthenticationManager(val openShiftClient: OpenShiftClient) : AuthenticationManager {

    companion object {
        private val headerPattern: Pattern = Pattern.compile("Bearer\\s+(.*)", Pattern.CASE_INSENSITIVE)

        private fun getBearerTokenFromAuthentication(authentication: Authentication?): String {
            val authenticationHeaderValue = authentication?.principal?.toString()
            val matcher = headerPattern.matcher(authenticationHeaderValue)
            if (!matcher.find()) {
                throw BadCredentialsException("Unexpected Authorization header format")
            }
            return matcher.group(1)
        }
    }

    override fun authenticate(authentication: Authentication?): Authentication {

        try {
            val token = getBearerTokenFromAuthentication(authentication)
            val user = openShiftClient.userToken(token).user().block() ?: BadCredentialsException("Could not find user")
            return PreAuthenticatedAuthenticationToken(user, token)
        } catch (e: Exception) {
            throw BadCredentialsException(e.localizedMessage, e)
        }
    }
}
