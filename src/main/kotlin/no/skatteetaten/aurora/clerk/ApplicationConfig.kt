package no.skatteetaten.aurora.clerk

import io.fabric8.openshift.client.DefaultOpenShiftClient
import io.fabric8.openshift.client.OpenShiftClient
import io.fabric8.openshift.client.OpenShiftConfigBuilder
import no.skatteetaten.aurora.boober.service.openshift.token.UserDetailsTokenProvider
import no.skatteetaten.aurora.clerk.service.openshift.token.ServiceAccountTokenProvider
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

enum class TokenTypes {
    USER, CLERK
}

@Target(AnnotationTarget.TYPE, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class TargetToken(val value: TokenTypes)

@Configuration

class ApplicationConfig : BeanPostProcessor {

    @Bean
    @TargetToken(TokenTypes.CLERK)
    fun client(tokenProvider: ServiceAccountTokenProvider): OpenShiftClient {
        return DefaultOpenShiftClient(
            OpenShiftConfigBuilder()
                .withOauthTokenProvider(tokenProvider)
                .build()
        )
    }

    @Bean
    @TargetToken(TokenTypes.USER)
    fun userClient(tokenProvider: UserDetailsTokenProvider): OpenShiftClient {
        return DefaultOpenShiftClient(
            OpenShiftConfigBuilder()
                .withOauthTokenProvider(tokenProvider)
                .build()
        )
    }
}
