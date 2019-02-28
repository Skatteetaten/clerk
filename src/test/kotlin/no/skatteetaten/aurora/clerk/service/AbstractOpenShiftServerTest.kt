package no.skatteetaten.aurora.clerk.service

import io.fabric8.openshift.client.server.mock.OpenShiftServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

open class AbstractOpenShiftServerTest {

    protected val openShiftServer = OpenShiftServer(false, true)

    @BeforeEach
    fun setUpOpenShiftServer() {
        openShiftServer.before()
    }

    @AfterEach
    fun tearDownOpenShiftServer() {
        openShiftServer.after()
    }
}