package com.app4.project.timelapseserver.integration.tests

import com.app4.project.timelapse.model.Execution
import com.app4.project.timelapseserver.integration.tests.util.RestResponseException
import groovy.time.TimeCategory
import org.apache.http.HttpStatus
import spock.lang.Shared
import spock.lang.Stepwise

@SuppressWarnings("GroovyVariableNotAssigned")
@Stepwise // allow to run tests in their definition order
class ExecutionControllerTest extends IntegrationTest {

    static final String EXECUTION_ENDPOINT = '/api/executions'
    
    @Shared
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

    def 'test get execution by id'() {
        when:
        def response = client.get(path: "$EXECUTION_ENDPOINT/0")
        then:
        assert response.data.id == 0
        assert response.status == HttpStatus.SC_OK
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
        println "Posted execution has id $postedExecutionId"
        then: 'server returns 200 code '
        assert response.status == HttpStatus.SC_OK
        def data = response.data
        assert execution.title == data.title
        assert execution.startTime == data.startTime
        assert execution.endTime == data.endTime
        assert execution.period == data.period
    }

    def 'test get execution current execution'() {
        when:
        def response = client.get(path: "$EXECUTION_ENDPOINT/current")
        then:
        assert response.status == HttpStatus.SC_OK
        assert response.data.id == postedExecutionId
    }

    def 'test post overlaping execution'() {
        setup:
        Execution execution = new Execution(title: "I overlaps", startTime: now(),
                endTime: now() + 1000, period: 8)
        when:
        client.post(path: EXECUTION_ENDPOINT, body: execution)
        then: 'server returns 409 code (conflict)'
        RestResponseException e = thrown(RestResponseException)
        assert e.statusCode == HttpStatus.SC_CONFLICT
    }

    def 'test patch execution 1 field'() {
        when:
        String newTitle = "hey new title"
        def patchResponse = client.patch(path: "$EXECUTION_ENDPOINT/$postedExecutionId", body: [title: newTitle])
        def getResponse = client.get(path: "$EXECUTION_ENDPOINT/$postedExecutionId")
        then:
        assert patchResponse.status == HttpStatus.SC_OK
        assert getResponse.status == HttpStatus.SC_OK
        assert getResponse.data == patchResponse.data
        def execution = getResponse.data
        assert execution.title == newTitle
    }

    def 'test patch execution 2 field'() {
        when:
        String newTitle = "This is new"
        long startTime = now()
        def patchResponse = client.patch(path: "$EXECUTION_ENDPOINT/$postedExecutionId",
                body: [title: newTitle, startTime: startTime])
        def getResponse = client.get(path: "$EXECUTION_ENDPOINT/$postedExecutionId")
        then:
        assert patchResponse.status == HttpStatus.SC_OK
        assert getResponse.status == HttpStatus.SC_OK
        def execution = getResponse.data
        assert execution.title == newTitle
        assert execution.startTime == startTime
    }

    def 'test patch execution 3 field'() {
        when:
        String newTitle = "This is reaaaaally new"
        long startTime = now() 
        long endTime = now() + 1000L
        def patchResponse = client.patch(path: "$EXECUTION_ENDPOINT/$postedExecutionId",
                body: [title: newTitle, startTime: startTime, endTime: endTime])
        def getResponse = client.get(path: "$EXECUTION_ENDPOINT/$postedExecutionId")
        then:
        assert patchResponse.status == HttpStatus.SC_OK
        assert getResponse.status == HttpStatus.SC_OK
        def execution = getResponse.data
        assert execution.title == newTitle
        assert execution.startTime == startTime
        assert execution.endTime == endTime
    }

    def 'test delete execution'() {
        when:
        def response = client.delete(path: "$EXECUTION_ENDPOINT/$postedExecutionId")
        then:
        assert response.status == HttpStatus.SC_OK
    }

    def 'test get execution not found'() {
        when:
        client.get(path: "$EXECUTION_ENDPOINT/$postedExecutionId")
        then:
        RestResponseException e = thrown(RestResponseException)
        assert e.response.status == HttpStatus.SC_NOT_FOUND
    }

    def cleanupSpec() {
        if (postedExecutionId != 0) {
            client.delete(path: "$EXECUTION_ENDPOINT/$postedExecutionId")
        }
    }
}