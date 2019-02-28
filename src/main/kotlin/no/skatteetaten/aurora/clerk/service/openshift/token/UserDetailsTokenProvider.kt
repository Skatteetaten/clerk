package no.skatteetaten.aurora.boober.service.openshift.token

import io.fabric8.kubernetes.client.OAuthTokenProvider
import no.skatteetaten.aurora.clerk.service.openshift.token.TokenProvider
import no.skatteetaten.aurora.clerk.service.openshift.token.UserDetailsProvider
import org.springframework.stereotype.Component

@Component
class UserDetailsTokenProvider(val userDetailsProvider: UserDetailsProvider) : TokenProvider, OAuthTokenProvider {

    override fun getToken(): String = userDetailsProvider.getAuthenticatedUser().token
}