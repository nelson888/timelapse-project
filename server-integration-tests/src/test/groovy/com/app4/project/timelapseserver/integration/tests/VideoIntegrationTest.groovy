package com.app4.project.timelapseserver.integration.tests

import com.app4.project.timelapse.model.TaskState
import com.app4.project.timelapseserver.integration.tests.util.RestResponseException
import groovyx.net.http.ContentType
import org.apache.http.HttpStatus
import spock.lang.Shared

import static com.app4.project.timelapseserver.integration.tests.ExecutionControllerTest.EXECUTION_ENDPOINT
import static com.app4.project.timelapseserver.integration.tests.ExecutionControllerTest.EXECUTION_ENDPOINT

import spock.lang.Stepwise

// testing all the process of creating a video and getting a video
@Stepwise // allow to run tests in their definition order
class VideoIntegrationTest extends IntegrationTest {

    private static final String VIDEO_ENDPOINT = '/storage/videos'
    private static final String IMAGE_ENDPOINT = '/storage/images'
    private static final String VIDEO_TASK_ENDPOINT = '/api/videos/tasks'
    private final int executionId = 1

    @Shared
    private int taskId
    @Shared
    private int videoId

    def 'create video task test'() {
        when:
        def response = client.post(path: "$EXECUTION_ENDPOINT/$executionId/video/generate")
        this.taskId = response.data.taskId
        then:
        assert response.status == HttpStatus.SC_OK
        def data = response.data
        assert data.state as TaskState == TaskState.ON_GOING
    }

    def 'get video task progress'() {
        when:
        def response = client.get(path: "$VIDEO_TASK_ENDPOINT/$taskId")
        then:
        assert response.status == HttpStatus.SC_OK
        def data = response.data
        assert data.state as TaskState == TaskState.ON_GOING
        assert data.percentage >= 0
        assert data.taskId == taskId
    }

    def 'create video task with wrong timestamps'() {
        when:
        long fromTimestamp = now()
        long toTimestamp = now() - 100000L
        def response = client.post(path: "$EXECUTION_ENDPOINT/$executionId/video/generate",
        query: [fromTimestamp: fromTimestamp, toTimestamp: toTimestamp])
        then:
        def data = response.data
        assert response.status == HttpStatus.SC_OK
        assert data.percentage == -1
        assert data.taskId == -1
        assert data.state as TaskState == TaskState.NOT_STARTED
    }

    def 'create video task with wrong fps'() {
        when:
        long fromTimestamp = now()
        long toTimestamp = now() + 100000L
        client.post(path: "$EXECUTION_ENDPOINT/$executionId/video/generate",
                query: [fromTimestamp: fromTimestamp, toTimestamp: toTimestamp, fps: -12])
        then:
        RestResponseException e = thrown(RestResponseException)
        def response = e.response
        assert response.status == HttpStatus.SC_BAD_REQUEST
    }

    private long getTimestampOfImage(int imageId) {
        return client.get(path: "$IMAGE_ENDPOINT/$executionId/$imageId/metadata")
                .data
                .uploadTimestamp
    }

    def 'create video task with valid timestamps frame'() {
        setup:
        long imagesCount = client.get(path: "$IMAGE_ENDPOINT/$executionId/count").data
        if (imagesCount<= 10) {
            throw new RuntimeException("We need at least 10 images to run this test")
        }
        long fromTimestamp = getTimestampOfImage((imagesCount * 0.2f).toInteger())
        long toTimestamp = getTimestampOfImage((imagesCount * 0.8f).toInteger())
        when:
        def response = client.post(path: "$EXECUTION_ENDPOINT/$executionId/video/generate",
                query: [fromTimestamp: fromTimestamp, toTimestamp: toTimestamp])
        then:
        assert response.status == HttpStatus.SC_OK
        def data = response.data
        assert data.state as TaskState == TaskState.ON_GOING
        assert data.percentage >= 0
    }

    def 'wait for task to finish'() {
        when:
        def response
        while (response == null || (response.data.state as TaskState) == TaskState.ON_GOING) {
            response = client.get(path: "$VIDEO_TASK_ENDPOINT/$taskId")
            println("Progress: ${response.data.percentage}%")
            Thread.sleep(1000L)
        }
        this.videoId = response.data.videoId
        then:
        assert response.status == HttpStatus.SC_OK
        def data = response.data
        assert data.state as TaskState == TaskState.FINISHED
        assert data.percentage == 100
        assert data.taskId == taskId
    }

    def 'get video metadata test'() {
        when:
        def response = client.get(path: "$VIDEO_ENDPOINT/$videoId/metadata")
        then:
        assert response.status == HttpStatus.SC_OK
        def data = response.data
        assert data.videoId == videoId
        assert data.executionId == executionId
        assert  data.framesCount > 0
    }

    def 'get video test'() {
        when:
        def response = client.get(path: "$VIDEO_ENDPOINT/$videoId", contentType: ContentType.BINARY)
        then:
        assert response.status == HttpStatus.SC_OK
        def data = response.data
        assert data instanceof InputStream
        data.close()
    }
    
    
}
