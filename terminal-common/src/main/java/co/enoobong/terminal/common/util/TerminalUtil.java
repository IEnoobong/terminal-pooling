package co.enoobong.terminal.common.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;

public final class TerminalUtil {
  private TerminalUtil() {
  }

  public static HttpStatus resolveAnnotatedResponseStatus(Exception exception) {
    final ResponseStatus annotation = findMergedAnnotation(exception.getClass(), ResponseStatus.class);
    if (annotation != null) {
      return annotation.value();
    } else {
      return HttpStatus.INTERNAL_SERVER_ERROR;
    }
  }
}
