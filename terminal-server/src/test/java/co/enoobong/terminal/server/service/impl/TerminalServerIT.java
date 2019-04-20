package co.enoobong.terminal.server.service.impl;

import co.enoobong.TerminalServerApp;
import co.enoobong.terminal.common.model.request.TerminalPayload;
import co.enoobong.terminal.common.model.response.TerminalResponse;
import com.googlecode.junittoolbox.MultithreadingTester;
import com.googlecode.junittoolbox.RunnableAssert;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.TimeoutRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import static co.enoobong.terminal.server.util.TestUtils.BASE_TERMINAL_PATH;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = TerminalServerApp.class)
@ContextConfiguration(classes = TerminalServerIT.TestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestExecutionListeners({DirtiesContextTestExecutionListener.class, DependencyInjectionTestExecutionListener.class})
public class TerminalServerIT {

  @LocalServerPort
  int randomServerPort;

  @Autowired
  private RetryTemplate retryTemplate;

  @Rule
  public final ExpectedException expectedException = ExpectedException.none();
  @Value("${terminal.available.period.seconds}")
  private long terminalAvailabilityPeriod;
  private RestTemplate testRestTemplate;

  @Before
  public void setup() {
    testRestTemplate = new RestTemplate();
    testRestTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory("http://localhost:" + randomServerPort));
  }


  @Test
  public void attemptToProcessTerminalAfterTimeFrameExpiredShouldFail() throws InterruptedException {
    final ResponseEntity<TerminalResponse> terminalResponse = testRestTemplate.getForEntity(BASE_TERMINAL_PATH + "/availableTerminalId", TerminalResponse.class);

    assertEquals(200, terminalResponse.getStatusCodeValue());

    TimeUnit.SECONDS.sleep(terminalAvailabilityPeriod);

    final TerminalPayload terminalPayload = new TerminalPayload(terminalResponse.getBody().getTerminalId(), 2,
            System.currentTimeMillis());

    expectedException.expect(isA(RestClientException.class));

    testRestTemplate.postForObject(BASE_TERMINAL_PATH, terminalPayload, String.class);
  }

  @Test
  public void concurrentUserTests() {
    final int numberOfClients = 30;

    final RunnableAssert runnableAssert = new RunnableAssert("get terminal id and process") {
      @Override
      public void run() {
        final ResponseEntity<TerminalResponse> terminalResponse =
                retryTemplate.execute((RetryCallback<ResponseEntity<TerminalResponse>, RestClientException>) context -> testRestTemplate.getForEntity(BASE_TERMINAL_PATH + "/availableTerminalId", TerminalResponse.class));

        assertEquals(200, terminalResponse.getStatusCodeValue());

        final TerminalPayload terminalPayload = new TerminalPayload(terminalResponse.getBody().getTerminalId(), 2,
                System.currentTimeMillis());

        final ResponseEntity<String> responseEntity = retryTemplate.execute(
                (RetryCallback<ResponseEntity<String>, RestClientException>) context ->
                        testRestTemplate.postForEntity(BASE_TERMINAL_PATH, terminalPayload, String.class));

        assertEquals(200, responseEntity.getStatusCodeValue());
      }
    };
    new MultithreadingTester().numThreads(numberOfClients).numRoundsPerThread(1).add(runnableAssert).run();
  }

  @EnableRetry
  @Configuration
  static class TestConfig {

    @Bean
    public RetryTemplate retryTemplate() {
      RetryTemplate retryTemplate = new RetryTemplate();

      FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
      fixedBackOffPolicy.setBackOffPeriod(5000L);
      retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

      TimeoutRetryPolicy retryPolicy = new TimeoutRetryPolicy();
      retryPolicy.setTimeout(TimeUnit.SECONDS.toMillis(30));

      retryTemplate.setRetryPolicy(retryPolicy);
      return retryTemplate;
    }
  }
}
