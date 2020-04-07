package com.app4.project.timelapseserver.integration.tests

import com.app4.project.timelapse.model.CameraState
import org.apache.http.HttpStatus
import spock.lang.Stepwise

@Stepwise
class CameraStateIntegrationTest extends IntegrationTest {

    private static final String CAMERA_STATE_ENDPOINT = '/api/state'
    
    private static final CameraState STATE = new CameraState(cameraWorking: true,
    sleeping: true, turnedOn: true, batteryPercentage: 55L)

    def 'test get camera state'() {
        when:
        def response = client.get(path: CAMERA_STATE_ENDPOINT)
        then:
        assert response.status == HttpStatus.SC_OK
        assert response.data != null
    }

    def 'test put camera state'() {
        when:
        def putResponse = client.put(path: CAMERA_STATE_ENDPOINT, body: STATE)
        def getResponse = client.get(path: CAMERA_STATE_ENDPOINT)
        then:
        assert putResponse.status == HttpStatus.SC_OK
        assert getResponse.status == HttpStatus.SC_OK
        assert getResponse.data == getResponse.data
        def data = getResponse.data as CameraState
        assert data.cameraWorking == STATE.cameraWorking
        assert data.sleeping == STATE.sleeping
        assert data.turnedOn == STATE.turnedOn
        assert data.batteryPercentage == STATE.batteryPercentage
        // the server should have updated the lastHeartBeat
        assert Math.abs(now() - data.lastHeartBeat) < 500L
    }
}
