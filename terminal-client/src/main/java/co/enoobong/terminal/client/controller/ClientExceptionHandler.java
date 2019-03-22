package co.enoobong.terminal.client.controller;

import co.enoobong.terminal.common.model.response.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientResponseException;

import static co.enoobong.terminal.common.util.TerminalUtil.resolveAnnotatedResponseStatus;

@ControllerAdvice
public class ClientExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(ClientExceptionHandler.class);

  @ExceptionHandler
  public ResponseEntity<?> handleExceptions(Exception exception) {
    log.error("Error occurred {}", exception.getMessage());
    if (exception instanceof RestClientResponseException) {
      final String responseBody = ((RestClientResponseException) exception).getResponseBodyAsString();
      return new ResponseEntity<>(responseBody, HttpStatus.SERVICE_UNAVAILABLE);
    }
    final MessageResponse errorResponse = new MessageResponse(exception.getMessage());
    final HttpStatus httpStatus = resolveAnnotatedResponseStatus(exception);
    return new ResponseEntity<>(errorResponse, httpStatus);
  }
}
