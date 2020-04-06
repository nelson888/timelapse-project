package com.app4.project.timelapseserver.integration.tests

import static com.app4.project.timelapseserver.integration.tests.ExecutionControllerTest.EXECUTION_ENDPOINT

import spock.lang.Stepwise

// testing all the process of creating a video
@Stepwise // allow to run tests in their definition order
class VideoIntegrationTest extends IntegrationTest {

    private static final String STORAGE_ENDPOINT = '/storage/videos'

    def 'create video test'() {
        when:
        client.get()
    }
}
