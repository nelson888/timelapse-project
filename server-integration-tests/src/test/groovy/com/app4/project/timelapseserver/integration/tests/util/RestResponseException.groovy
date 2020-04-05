package com.app4.project.timelapseserver.integration.tests.util

import groovyx.net.http.HttpResponseDecorator

class RestResponseException extends IOException {
    final HttpResponseDecorator response

    static String getMessage(HttpResponseDecorator response) {
        def responseBody = response.data
        if (responseBody && responseBody.title) {
            return "[${response.status}] ${responseBody.title}: ${responseBody.message}"
        }
        return responseBody
    }

    RestResponseException(Throwable e, HttpResponseDecorator response) {
        super(getMessage(response), e)
        this.response = response
    }

    int getStatusCode() {
        return response.status
    }

}
