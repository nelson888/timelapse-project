package com.app4.project.timelapseserver.integration.tests

import com.app4.project.timelapse.model.Execution
import com.app4.project.timelapseserver.integration.tests.util.RestResponseException
import groovy.time.TimeCategory
import org.apache.http.HttpStatus
import spock.lang.Stepwise

@SuppressWarnings("GroovyVariableNotAssigned")
@Stepwise // allow to run tests in their definition order
class ExecutionControllerTest extends IntegrationTest {

    private static final String EXECUTION_ENDPOINT = '/api/executions'
    private int postedExecutionId

    def 'get all executions'() {
        when:
        def response = client.get(path: '/api/executions')

        then:
        assert response.status == 200
        List<Execution> executions = response.data as List<Execution>
        println executions
        assert executions != null

    }

    def 'test post execution with no title'() {
        setup:
        Execution execution = new Execution(title: null, startTime: now(),
                endTime: now() + 1000, period: 8)
        when:
        client.post(path: EXECUTION_ENDPOINT, body: execution)
        then: 'server returns 400 code (bad request)'
        RestResponseException e = thrown(RestResponseException)
        assert e.statusCode == HttpStatus.SC_BAD_REQUEST
    }

    def 'test post execution with reversed start and end time'() {
        setup:
        Execution execution = new Execution(title: null, startTime: now() + 5000,
                endTime: now(), period: 8)
        when:
        client.post(path: EXECUTION_ENDPOINT, body: execution)
        then: 'server returns 400 code (bad request)'
        RestResponseException e = thrown(RestResponseException)
        assert e.statusCode == HttpStatus.SC_BAD_REQUEST
    }

    def 'test post execution with bad period'() {
        setup:
        Execution execution = new Execution(title: null, startTime: now(),
                endTime: now() + 1000, period: 0)
        when:
        client.post(path: EXECUTION_ENDPOINT, body: execution)
        then: 'server returns 400 code (bad request)'
        RestResponseException e = thrown(RestResponseException)
        assert e.statusCode == HttpStatus.SC_BAD_REQUEST
    }

    def 'test post execution'() {
        setup:
        Execution execution
        use(TimeCategory) {
            execution = new Execution(title: "Some new execution", startTime: now(),
                    endTime: now() + 5.hours.toMilliseconds(), period: 8)
        }
        when:
        def response = client.post(path: EXECUTION_ENDPOINT, body: execution)
        postedExecutionId = response.data.id // will be used for delete test
        then: 'server returns 200 code '
        assert response.status == HttpStatus.SC_OK
    }

    // TODO add overlap test
    def 'test delete execution'() {
        when:
        def response = client.delete(path: "$EXECUTION_ENDPOINT/$postedExecutionId")
        then:
        assert response.status == HttpStatus.SC_OK
    }
}