package no.skatteetaten.aurora.clerk.service

import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import com.fkorotkov.kubernetes.extensions.newScale
import com.fkorotkov.kubernetes.extensions.spec
import com.fkorotkov.kubernetes.metadata
import com.fkorotkov.kubernetes.newPod
import com.fkorotkov.openshift.metadata
import com.fkorotkov.openshift.newDeploymentConfig
import com.fkorotkov.openshift.spec
import io.mockk.every
import io.mockk.mockk
import java.time.Instant
import no.skatteetaten.aurora.clerk.controller.PodItem
import no.skatteetaten.aurora.clerk.controller.ScaleCommand
import no.skatteetaten.aurora.mockmvc.extensions.mockwebserver.HttpMock
import no.skatteetaten.aurora.openshift.webclient.DEPLOYMENT_CONFIG_ANNOTATION
import no.skatteetaten.aurora.openshift.webclient.KubernetesApiGroup.POD
import no.skatteetaten.aurora.openshift.webclient.OpenShiftApiGroup.DEPLOYMENTCONFIG
import no.skatteetaten.aurora.openshift.webclient.OpenShiftApiGroup.DEPLOYMENTCONFIGSCALE
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.web.reactive.function.client.WebClientResponseException

class DeploymentConfigServiceTest : AbstractOpenShiftServerTest() {

    private val podService: PodService = mockk()
    private val dcService = DeploymentConfigService(openShiftClient, podService)

    private val env = "jedi-test"
    private val name = "luke"
    private val dcName = "starwars"
    private val started = Instant.now().toString()

    private val luke = PodItem("$name-1", name, started, "Running")
    private val command = ScaleCommand(name = name, replicas = 1)

    @Test
    fun `scale single item`() {

        every { podService.getPodItems(env, name) } returns listOf(luke)

        openShiftMock {
            rule {
                """{}""".toJsonBody()
            }
        }

        val result = dcService.scale(command, env, 100)
        assertThat(result.pods.size).isEqualTo(1)
    }

    @Test
    fun `scale single item fails http communication`() {

        openShiftMock {
            rule {
                MockResponse().setBody("not found").setResponseCode(404)
            }
        }

        assertThat {
            dcService.scale(command, env, 100)
        }.isFailure().isInstanceOf(WebClientResponseException.NotFound::class)
    }

    @Test
    fun `delete pod then reduce (scale) replicas with one`() {

        openShiftMock {
            getPodMockRule()

            rule({ matchMethodAndEndpoint(HttpMethod.GET, DEPLOYMENTCONFIG.uri(env, dcName)) }) {
                newDeploymentConfig {
                    metadata {
                        name = dcName
                        namespace = env
                    }
                    spec {
                        replicas = 2
                    }
                }.toJsonBody()
            }

            rule({ matchMethodAndEndpoint(HttpMethod.DELETE, POD.uri(env, luke.name)) }) {
                MockResponse()
            }

            rule({ matchMethodAndEndpoint(HttpMethod.PUT, DEPLOYMENTCONFIGSCALE.uri(env, dcName)) }) {
                newScale {
                    spec {
                        replicas = 1
                    }
                }.toJsonBody()
            }
        }

        val result = dcService.deletePodAndScaleDown(env, luke.name)
        assertThat(result.currentReplicas).isEqualTo(1)
        assertThat(result.deletedPodName).isEqualTo(luke.name)
        assertThat(result.scaleResult).isNotNull()
    }

    @Test
    fun `should fail when Pod is not found`() {

        openShiftMock {
            rule({ matchMethodAndEndpoint(HttpMethod.GET, POD.uri(env, luke.name)) }) {
                MockResponse().setResponseCode(404)
            }
        }

        assertThat {
            dcService.deletePodAndScaleDown(env, luke.name)
        }.isFailure().hasMessage("Cannot find Pod ${luke.name} in $env")
    }

    @Test
    fun `should fail when DeploymentConfig is not found`() {
        openShiftMock {
            getPodMockRule()
            rule({ matchMethodAndEndpoint(HttpMethod.GET, DEPLOYMENTCONFIG.uri(env, dcName)) }) {
                MockResponse().setResponseCode(404)
            }
        }

        assertThat {
            dcService.deletePodAndScaleDown(env, luke.name)
        }.isFailure().hasMessage("Cannot find DeploymentConfig $dcName in $env")
    }

    @Test
    fun `should fail when DeploymentConfig annotation for pod is missing`() {
        openShiftMock {
            getPodMockRule {
                newPod {
                    metadata {
                        name = luke.name
                        namespace = env
                        annotations = emptyMap()
                    }
                }.toJsonBody()
            }
        }

        assertThat {
            dcService.deletePodAndScaleDown(env, luke.name)
        }.isFailure().hasMessage("Pod ${luke.name} does'nt have a DeploymentConfig annotation")
    }

    private fun HttpMock.getPodMockRule(fn: () -> MockResponse = ::createPod) = this.rule({ matchMethodAndEndpoint(HttpMethod.GET, POD.uri(env, luke.name)) }) {
        fn()
    }

    private fun createPod() = newPod {
            metadata {
                name = luke.name
                namespace = env
                annotations = mapOf(
                    DEPLOYMENT_CONFIG_ANNOTATION to dcName
                )
            }
        }.toJsonBody()
}
