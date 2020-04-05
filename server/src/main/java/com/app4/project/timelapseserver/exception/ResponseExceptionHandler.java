package com.app4.project.timelapseserver.exception;

import com.app4.project.timelapse.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ResponseExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(FileNotFoundException.class)
  public final ResponseEntity<ErrorResponse> fileNotFoundException(FileNotFoundException ex, WebRequest request) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
      .body(new ErrorResponse("File not found", ex.getMessage()));
  }

  @ExceptionHandler(NotFoundException.class)
  public final ResponseEntity<ErrorResponse> notFoundException(NotFoundException ex, WebRequest request) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
      .body(new ErrorResponse("Not Found", ex.getMessage()));
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

  @ExceptionHandler(ConflictException.class)
  public final ResponseEntity<ErrorResponse> conflictException(ConflictException ex, WebRequest request) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
      .body(new ErrorResponse("A conflict occurred", ex.getMessage()));
  }

  @ExceptionHandler(MultipartException.class)
  public final ResponseEntity<ErrorResponse> multipartException(MultipartException ex, WebRequest request) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
      .body(new ErrorResponse("Multipart exception", ex.getMessage()));
  }

}
