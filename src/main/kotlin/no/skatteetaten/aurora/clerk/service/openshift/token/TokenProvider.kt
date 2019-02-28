package no.skatteetaten.aurora.clerk.service.openshift.token

interface TokenProvider {
    fun getToken(): String
}