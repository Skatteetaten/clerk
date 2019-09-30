package no.skatteetaten.aurora.clerk

import no.skatteetaten.aurora.openshift.webclient.OpenShiftClientConfig
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(OpenShiftClientConfig::class)
class Main

fun main(args: Array<String>) {

    SpringApplication.run(Main::class.java, *args)
}
