package no.skatteetaten.aurora.clerk.contracts

import com.nhaarman.mockito_kotlin.any
import no.skatteetaten.aurora.clerk.controller.security.User
import no.skatteetaten.aurora.clerk.service.PodService
import no.skatteetaten.aurora.clerk.service.openshift.token.UserDetailsProvider
import org.junit.jupiter.api.BeforeEach
import org.mockito.BDDMockito.given
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.test.context.support.WithMockUser

@WithMockUser
class PodBase : ContractBase() {

    @MockBean
    private lateinit var podService: PodService


    @MockBean
    private lateinit var userDetailsProvider: UserDetailsProvider

    @BeforeEach
    fun setUp() {
        withContractResponses(this) {
            given(userDetailsProvider.getAuthenticatedUser()).willReturn(User("system:serviceaccount:jedi:default", "token"))
            given(podService.getPodItems(any(), any())).willReturn(it.response())
        }
    }
}