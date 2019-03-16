package com.app4.project.timelapseserver.exception;

import com.app4.project.timelapse.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ResponseExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(FileNotFoundException.class)
  public final ResponseEntity<ErrorResponse> fileNotFoundException(FileNotFoundException ex, WebRequest request) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse("File not found", ex.getMessage()));
  }

  @ExceptionHandler(FileStorageException.class)
  public final ResponseEntity<ErrorResponse> fileStorageException(FileStorageException ex, WebRequest request) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse("File storage error", ex.getMessage()));
  }

  @ExceptionHandler(BadRequestException.class)
  public final ResponseEntity<ErrorResponse> badRequestException(BadRequestException ex, WebRequest request) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse("Bad request", ex.getMessage()));
  }

  @ExceptionHandler(BadCredentialsException.class)
  public final ResponseEntity<ErrorResponse> authenticationException(BadCredentialsException ex, WebRequest request) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
      .body(new ErrorResponse("Bad authentication", ex.getMessage()));
  }

  @ExceptionHandler(InvalidJwtAuthenticationException.class)
  public final ResponseEntity<ErrorResponse> authenticationException(InvalidJwtAuthenticationException ex, WebRequest request) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
      .body(new ErrorResponse("Bad authentication", ex.getMessage()));
  }

}
