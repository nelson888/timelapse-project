package com.app4.project.timelapseserver.integration.tests

import com.app4.project.timelapseserver.integration.tests.util.CustomRestClient
import spock.lang.Shared
import spock.lang.Specification

abstract class IntegrationTest extends Specification {

    @Shared
    def client = new CustomRestClient('http://localhost:8080/')

    static long now() {
        return System.currentTimeMillis()
    }

}
