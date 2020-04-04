package com.app4.project.timelapseserver.integration.tests

import com.app4.project.timelapse.model.Execution
import groovy.time.TimeCategory
import groovyx.net.http.HttpResponseException
import org.apache.http.HttpStatus

@SuppressWarnings("GroovyVariableNotAssigned")
class ExecutionControllerTest extends IntegrationTest {

    def 'get all executions'() {
        when:
        def response = client.get(path: '/api/executions')

        then:
        assert response.status == 200
        List<Execution> executions =  response.data as List<Execution>
        assert executions.size() == 4

    }

    def 'test post execution with no title'() {
        when:
        Execution execution = new Execution(title: null, startTime: now(),
                endTime: now() + 1000)
        client.post(path: '/api/executions', body: execution)
        then: 'server returns 400 code (bad request)'
        HttpResponseException e = thrown(HttpResponseException)
        assert HttpStatus.SC_BAD_REQUEST == e.statusCode
    }

    def 'test post execution with reversed start and end time'() {
        when:
        Execution execution = new Execution(title: null, startTime: now() + 5000,
                endTime: now())
        client.post(path: '/api/executions', body: execution)
        then: 'server returns 400 code (bad request)'
        HttpResponseException e = thrown(HttpResponseException)
        assert HttpStatus.SC_BAD_REQUEST == e.statusCode
    }



    def 'test post execution'() {
        when:
        Execution execution
        use(TimeCategory) {
            execution = new Execution(title: null, startTime: now(),
                    endTime: now() + 5.hours.toMilliseconds())
        }
        if (false) // TODO
        client.post(path: '/api/executions', body: execution)
        then: 'server returns 200 code '
        assert true
        // TODO
    }
}
