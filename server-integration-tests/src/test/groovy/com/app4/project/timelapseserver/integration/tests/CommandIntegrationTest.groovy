package com.app4.project.timelapseserver.integration.tests

import com.app4.project.timelapse.model.Command
import com.app4.project.timelapse.model.request.CommandPostRequest
import com.app4.project.timelapseserver.integration.tests.util.RestResponseException
import org.apache.http.HttpStatus
import spock.lang.Stepwise

@Stepwise // allow to run tests in their definition order
class CommandIntegrationTest extends IntegrationTest {
    
    private static final String COMMAND_ENDPOINT = '/api/commands'

    def 'test add command'() {
        when:
        def response = client.post(path: COMMAND_ENDPOINT, body: new CommandPostRequest(Command.SLEEP))
        then:
        assert response.status == HttpStatus.SC_OK
    }

    def 'test consume command execution by id'() {
        when:
        def response = client.get(path: "$COMMAND_ENDPOINT/consume")
        then:
        assert response.data as Command == Command.SLEEP
        assert response.status == HttpStatus.SC_OK
    }

    def 'test add null command'() {
        when:
        client.post(path: COMMAND_ENDPOINT, body: [command: null])
        then:
        RestResponseException e = thrown(RestResponseException)
        assert e.response.status == HttpStatus.SC_BAD_REQUEST
    }

    def 'test add unknown command'() {
        when:
        client.post(path: COMMAND_ENDPOINT, body: [command: "unknown"])
        then:
        RestResponseException e = thrown(RestResponseException)
        assert e.response.status == HttpStatus.SC_BAD_REQUEST
    }

    def 'test add and consume multiple commands'() {
        when:
        client.post(path: COMMAND_ENDPOINT, body: [command: Command.SLEEP])
        client.post(path: COMMAND_ENDPOINT, body: [command: Command.WAKE_UP])
        client.post(path: COMMAND_ENDPOINT, body: [command: Command.TURN_OFF])
        def commands = (1..3).collect { client.get(path: "$COMMAND_ENDPOINT/consume").data as Command }
        then:
        assert commands == [Command.SLEEP, Command.WAKE_UP, Command.TURN_OFF]
    }

}
