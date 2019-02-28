package no.skatteetaten.aurora.clerk.service

import no.skatteetaten.aurora.clerk.service.openshift.token.TokenProvider
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureStubRunner
class PodServiceTest {

    @Autowired
    private lateinit var renewService: PodService

    @MockBean
    @Suppress("unused")
    private lateinit var tokenProvider: TokenProvider

    @Test
    fun `fetch pods`() {
    }
}