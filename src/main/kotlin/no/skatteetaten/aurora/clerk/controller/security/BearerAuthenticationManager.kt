package no.skatteetaten.aurora.clerk.controller.security

import java.util.regex.Pattern
import mu.KotlinLogging
import no.skatteetaten.aurora.openshift.webclient.OpenShiftClient
import no.skatteetaten.aurora.openshift.webclient.blockForResource
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.stereotype.Component

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
            val user = openShiftClient.userToken(token).user().blockForResource() ?: BadCredentialsException("Could not find user")
            return PreAuthenticatedAuthenticationToken(user, token)
        } catch (e: Exception) {
            logger.warn("Feil under autentisering av bruker error=${e.localizedMessage}", e)
            throw BadCredentialsException(e.localizedMessage, e)
        }
    }
}
