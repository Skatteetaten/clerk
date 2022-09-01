package no.skatteetaten.aurora.clerk.controller.security

import javax.servlet.http.HttpServletRequest
import mu.KotlinLogging
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter
import org.springframework.security.web.util.matcher.RequestMatcher

private val logger = KotlinLogging.logger {}

@EnableWebSecurity
class WebSecurityConfig(
    val authenticationManager: BearerAuthenticationManager,
    @Value("\${management.server.port}") val managementPort: Int
) : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {

        http.csrf().disable()
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)

        http.authenticationProvider(preAuthenticationProvider())
            .addFilter(requestHeaderAuthenticationFilter())
            .authorizeRequests()
            // EndpointRequest.toAnyEndpoint() points to all actuator endpoints and then permitAll requests
            .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
            .antMatchers("/api/**").authenticated()
            .anyRequest().permitAll()
    }

    @Bean
    internal fun preAuthenticationProvider() = PreAuthenticatedAuthenticationProvider().apply {
        setPreAuthenticatedUserDetailsService {

            val principal = it.principal as io.fabric8.openshift.api.model.User
            val username = principal.metadata.name

            MDC.put("user", username)
            User(username, it.credentials as String).also {
                logger.info("Logged in user username=$username, tokenSnippet=${it.tokenSnippet}")
            }
        }
    }

    @Bean
    internal fun requestHeaderAuthenticationFilter() = RequestHeaderAuthenticationFilter().apply {
        setPrincipalRequestHeader("Authorization")
        setExceptionIfHeaderMissing(false)
        setAuthenticationManager(authenticationManager)
    }
}
