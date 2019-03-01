package no.skatteetaten.aurora.clerk.contracts

import com.nhaarman.mockito_kotlin.any
import no.skatteetaten.aurora.clerk.controller.PodItem
import no.skatteetaten.aurora.clerk.controller.PodResourceAssembler
import no.skatteetaten.aurora.clerk.controller.security.User
import no.skatteetaten.aurora.clerk.service.PodService
import no.skatteetaten.aurora.clerk.service.openshift.token.UserDetailsProvider
import org.junit.jupiter.api.BeforeEach
import org.mockito.BDDMockito.given
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.test.context.support.WithMockUser

@WithMockUser
open class PodBase : ContractBase() {

    @MockBean
    private lateinit var podService: PodService

    @MockBean
    private lateinit var assembler: PodResourceAssembler

    // TODO kan vi bruke samme metode som i controller testene her og slippe å mocke userDetailsProvider?
    @MockBean
    private lateinit var userDetailsProvider: UserDetailsProvider

    @BeforeEach
    fun setUp() {
        withContractResponses(this) {
            given(userDetailsProvider.getAuthenticatedUser()).willReturn(
                User(
                    "system:serviceaccount:jedi-test:default",
                    "token"
                )
            )
            given(assembler.toAuroraResource<PodItem>(any(), any())).willReturn(it.response())
        }
    }
}