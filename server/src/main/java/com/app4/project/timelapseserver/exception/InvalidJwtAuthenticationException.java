package com.app4.project.timelapseserver.exception;

import org.springframework.security.authentication.BadCredentialsException;

public class InvalidJwtAuthenticationException extends BadCredentialsException {
  public InvalidJwtAuthenticationException(String msg, Throwable e) {
    super(msg, e);
  }
}
