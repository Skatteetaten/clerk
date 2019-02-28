package no.skatteetaten.aurora.clerk.contracts

import com.nhaarman.mockito_kotlin.any
import no.skatteetaten.aurora.clerk.service.PodService
import org.junit.jupiter.api.BeforeEach
import org.mockito.BDDMockito.given
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.test.context.support.WithMockUser

@WithMockUser
class PodBase : ContractBase() {

    @MockBean
    private lateinit var podService: PodService

    @BeforeEach
    fun setUp() {
        withContractResponses(this) {
            given(podService.getPodItems(any(), any())).willReturn(it.response())
        }
    }
}