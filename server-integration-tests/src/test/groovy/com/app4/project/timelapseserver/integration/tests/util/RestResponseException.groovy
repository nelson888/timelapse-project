package com.app4.project.timelapseserver.integration.tests.util

import groovyx.net.http.HttpResponseDecorator

class RestResponseException extends IOException {
    final HttpResponseDecorator response

    RestResponseException(Throwable e, HttpResponseDecorator response, Object responseBody) {
        this(e, response, "[${response.status}] ${responseBody.title}: ${responseBody.message}")
    }
    RestResponseException(Throwable e, HttpResponseDecorator response, String message) {
        super(message, e)
        this.response = response
    }

    int getStatusCode() {
        return response.status
    }

}
