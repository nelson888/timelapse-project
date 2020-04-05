package com.app4.project.timelapseserver.integration.tests.util

import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.apache.http.client.ClientProtocolException

class CustomRestClient extends RESTClient {
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
                throw new RestResponseException(e, e.response)
            }
        }
    }