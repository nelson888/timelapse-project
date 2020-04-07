package com.app4.project.timelapseserver.integration.tests

import com.app4.project.timelapseserver.integration.tests.util.RestResponseException
import groovyx.net.http.ContentType
import org.apache.http.HttpStatus
import spock.lang.Shared
import spock.lang.Stepwise

import javax.imageio.ImageIO

@Stepwise // allow to run tests in their definition order
class ImageStorageIntegrationTest extends IntegrationTest {

    private static final String STORAGE_ENDPOINT = '/storage/images'
    private static final String IMAGE_CONTENT_TYPE = 'image/jpeg'

    @Shared
    private int imageCount
    @Shared
    private long imageFileSize

    def 'get images count of execution'() {
        when:
        def response = client.get(path: "$STORAGE_ENDPOINT/1/count")
        imageCount = response.data
        then:
        assert response.status == HttpStatus.SC_OK
        assert imageCount > 0
    }

    def 'get first image of execution'() {
        when:
        def response = client.get(path: "$STORAGE_ENDPOINT/1/0", contentType: ContentType.BINARY)
        byte[] bytes = response.data.bytes
        imageFileSize = bytes.length
        then:
        assert response.status == HttpStatus.SC_OK
        assert response.contentType == IMAGE_CONTENT_TYPE
        assert response.data != null
        // test that image is parsable
        assert ImageIO.read(new ByteArrayInputStream(bytes)) != null

    }

    def 'get first image metadata of execution'() {
        when:
        def response = client.get(path: "$STORAGE_ENDPOINT/1/0/metadata")
        then:
        assert response.status == HttpStatus.SC_OK
        def metadata = response.data
        assert metadata.executionId == 1
        assert metadata.fileId == 0
        assert imageFileSize == metadata.size
    }

    def 'get last image of execution'() {
        when:
        def response = client.get(path: "$STORAGE_ENDPOINT/1/${imageCount - 1}", contentType: ContentType.BINARY)
        then:
        assert response.status == HttpStatus.SC_OK
        assert response.contentType == IMAGE_CONTENT_TYPE
        assert response.data != null
    }

    def 'get non existing image of execution'() {
        when:
        client.get(path: "$STORAGE_ENDPOINT/1/$imageCount")
        then:
        RestResponseException e = thrown(RestResponseException)
        assert e.response.status == HttpStatus.SC_NOT_FOUND
    }

    def 'post image test'() {
        when:
        def imageBytes = ImageStorageIntegrationTest.class.getResource('/image.jpg').newInputStream().bytes
        // TODO
        def response = client.postMultipart(path: "$STORAGE_ENDPOINT/1", body: imageBytes)
        then:
        def metadata = response.data
        assert response.status == HttpStatus.SC_CREATED
        assert metadata.executionId == 1
        assert metadata.size == imageBytes.length
        assert metadata.fileId == imageCount
    }
}