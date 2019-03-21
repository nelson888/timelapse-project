package com.app4.project.timelapseserver.controller;

import com.app4.project.timelapse.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

  public static final String ERROR_ENDPOINT = "/error";

  @RequestMapping(ERROR_ENDPOINT)
  public ResponseEntity handleError(HttpServletRequest request) {
    Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
    ErrorResponse response;
    if (statusCode == HttpStatus.UNAUTHORIZED.value() ||
    statusCode == HttpStatus.FORBIDDEN.value()) {
      response = new ErrorResponse("Not authorized", "You are not well authenticated");
    } else {
      response = new ErrorResponse("Error", "Error");
    }
    return ResponseEntity.status(statusCode)
      .body(response);
  }

  @Override
  public String getErrorPath() {
    return ERROR_ENDPOINT;
  }
}
