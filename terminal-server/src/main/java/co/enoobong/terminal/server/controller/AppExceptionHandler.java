package co.enoobong.terminal.server.controller;

import co.enoobong.terminal.common.model.response.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static co.enoobong.terminal.common.util.TerminalUtil.resolveAnnotatedResponseStatus;

@ControllerAdvice
public class AppExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(AppExceptionHandler.class);

  @ExceptionHandler
  public ResponseEntity<MessageResponse> handleExceptions(Exception exception) {
    log.error("Error occurred {}", exception.getMessage());
    final HttpStatus httpStatus = resolveAnnotatedResponseStatus(exception);
    final MessageResponse errorResponse = new MessageResponse(exception.getMessage());
    return new ResponseEntity<>(errorResponse, httpStatus);
  }
}
