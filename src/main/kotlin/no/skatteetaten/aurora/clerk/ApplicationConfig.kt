package no.skatteetaten.aurora.clerk

import io.fabric8.openshift.client.DefaultOpenShiftClient
import io.fabric8.openshift.client.OpenShiftClient
import io.fabric8.openshift.client.OpenShiftConfigBuilder
import no.skatteetaten.aurora.clerk.service.openshift.token.ServiceAccountTokenProvider
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApplicationConfig : BeanPostProcessor {

    @Bean
    fun client(tokenProvider: ServiceAccountTokenProvider): OpenShiftClient {
        return DefaultOpenShiftClient(
            OpenShiftConfigBuilder()
                .withOauthTokenProvider(tokenProvider)
                .build()
        )
    }
}
