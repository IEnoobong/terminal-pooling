package co.enoobong.terminal.client.config;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.TimeoutRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

@EnableRetry
@Configuration
@PropertySource("classpath:terminal-client.properties")
public class TerminalClientConfig {

  @Value("${terminal.server.url}")
  private String terminalServerUrl;

  @Value("${terminal.retry.timeout.seconds}")
  private int retryTimeout;

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.rootUri(terminalServerUrl).build();
  }

  @Bean
  public RetryTemplate retryTemplate() {
    RetryTemplate retryTemplate = new RetryTemplate();

    FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
    fixedBackOffPolicy.setBackOffPeriod(5000L);
    retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

    TimeoutRetryPolicy retryPolicy = new TimeoutRetryPolicy();
    retryPolicy.setTimeout(TimeUnit.SECONDS.toMillis(retryTimeout));
    retryTemplate.setRetryPolicy(retryPolicy);

    return retryTemplate;
  }
}
