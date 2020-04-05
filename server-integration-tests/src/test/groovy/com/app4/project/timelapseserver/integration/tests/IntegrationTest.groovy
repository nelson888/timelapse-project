package com.app4.project.timelapseserver.integration.tests

import com.app4.project.timelapseserver.integration.tests.util.RestResponseException
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.apache.http.client.ClientProtocolException
import spock.lang.Shared
import spock.lang.Specification


// super class with some cool methods
abstract class IntegrationTest extends Specification {

    @Shared
    def client = new CustomRestClient( 'http://localhost:8080/')

    static long now() {
        return System.currentTimeMillis()
    }

    static class CustomRestClient extends RESTClient {
        CustomRestClient(def baseUri) {
            super(baseUri)
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
            return super.patch(args)
        }

        @Override
        HttpResponseDecorator post(Map<String, ?> args) throws URISyntaxException, ClientProtocolException, IOException {
            args.requestContentType = ContentType.JSON
            return super.post(args)
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
                throw new RestResponseException(e, e.response, e.response.data)
            }
        }
    }
}
