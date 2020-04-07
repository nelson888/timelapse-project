package com.app4.project.timelapseserver.integration.tests.util

import groovy.json.JsonSlurper
import groovyx.net.http.*
import org.apache.http.client.ClientProtocolException
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.ByteArrayBody

class CustomRestClient extends RESTClient {
    final String baseUri
   
    CustomRestClient(String baseUri) {
        super(baseUri)
        this.baseUri = baseUri
    }

    @Override
    HttpResponseDecorator get(Map<String, ?> args) throws ClientProtocolException, IOException, URISyntaxException {
        return super.get(args)
    }

    @Override
    HttpResponseDecorator delete(Map<String, ?> args) throws URISyntaxException, ClientProtocolException, IOException {
        return super.delete(args)
    }

    @Override
    HttpResponseDecorator patch(Map<String, ?> args) throws URISyntaxException, ClientProtocolException, IOException {
        args.requestContentType = ContentType.JSON
        return super.patch(args)
    }

    @Override
    HttpResponseDecorator post(Map<String, ?> args) throws URISyntaxException, ClientProtocolException, IOException {
        args.requestContentType = ContentType.JSON
        return super.post(args)
    }

    def postMultipart(Map<String, ?> args) throws URISyntaxException, ClientProtocolException, IOException {
        String endpoint = args.path
        if (endpoint.startsWith('/')) {
            endpoint = endpoint.substring(1)
        }
        URL url = new URL("$baseUri$endpoint")
        HttpURLConnection connection = (HttpURLConnection) url.openConnection()
        connection.setDoOutput(true)
        connection.setRequestMethod("POST")

        ByteArrayBody bytesBody = new ByteArrayBody(args.body, "multipart/form-data", "image.jpg")
        MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.STRICT)
        multipartEntity.addPart("image", bytesBody)

        connection.setRequestProperty("Content-Type", multipartEntity.getContentType().getValue())
        OutputStream out = connection.getOutputStream()
        try {
            multipartEntity.writeTo(out)
        } finally {
            out.close()
        }
        int status = connection.getResponseCode()
        def stream = connection.inputStream
        if (stream == null) {
            stream = connection.errorStream
        }
        def data = new JsonSlurper().parseText(stream.text)
        return [status:status, data: data]
    }

    @Override
    HttpResponseDecorator put(Map<String, ?> args) throws URISyntaxException, ClientProtocolException, IOException {
        args.requestContentType = ContentType.JSON
        return super.put(args)
    }

    @Override
    protected Object doRequest(HTTPBuilder.RequestConfigDelegate delegate) throws ClientProtocolException, IOException {
        try {
            return super.doRequest(delegate)
        } catch (HttpResponseException e) {
            throw new RestResponseException(e, e.response)
        }
    }
}