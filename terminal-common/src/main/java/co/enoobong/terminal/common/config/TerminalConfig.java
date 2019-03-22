package co.enoobong.terminal.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:terminal.properties")
public class TerminalConfig {

  @Value("${sequence.start}")
  private int start;

  @Value("${sequence.end}")
  private int end;

  public int getStart() {
    return start;
  }

  public int getEnd() {
    return end;
  }
}
