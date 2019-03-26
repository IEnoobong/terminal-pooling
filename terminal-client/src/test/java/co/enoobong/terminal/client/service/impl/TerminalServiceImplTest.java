package co.enoobong.terminal.client.service.impl;

import co.enoobong.terminal.client.util.SequenceGenerator;
import co.enoobong.terminal.common.model.request.TerminalPayload;
import co.enoobong.terminal.common.model.response.TerminalResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryState;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TerminalServiceImplTest {

  private SequenceGenerator sequenceGenerator = mock(SequenceGenerator.class);
  private RestTemplate restTemplate = mock(RestTemplate.class);
  private RetryTemplate retryTemplate = mock(RetryTemplate.class);

  private TerminalServiceImpl terminalService;

  @Before
  public void setup() {
    terminalService = new TerminalServiceImpl(sequenceGenerator, restTemplate, retryTemplate);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void processesRequest() {
    final TerminalResponse terminalResponse = new TerminalResponse("1001");
    final String expectedResponse = "success";
    final RetryCallback<?, RestClientException> getAvailableIdCallback = any(RetryCallback.class);
    final ArgumentCaptor<RetryCallback<?, RestClientException>> argumentCaptor = ArgumentCaptor.forClass(RetryCallback.class);
    final ArgumentCaptor<RecoveryCallback> recoveryCallbackCaptor = ArgumentCaptor.forClass(RecoveryCallback.class);
    final ArgumentCaptor<RetryState> retryStateCaptor = ArgumentCaptor.forClass(RetryState.class);

    given(retryTemplate.execute(getAvailableIdCallback, any(), any())).willAnswer(invocation -> {
      final RetryCallback retryCallback = invocation.getArgument(0);
      return retryCallback.doWithRetry(null);
    });

    given(restTemplate.getForObject(anyString(), any(Class.class))).willReturn(terminalResponse);
    given(restTemplate.postForObject(anyString(), any(TerminalPayload.class), any(Class.class))).willReturn(expectedResponse);


    final String response = terminalService.processRequest(null);

    assertEquals(expectedResponse, response);

    verify(sequenceGenerator).getNext();
    verify(retryTemplate, times(2)).execute(argumentCaptor.capture(), recoveryCallbackCaptor.capture(),
            retryStateCaptor.capture());

    assertNotNull(argumentCaptor.getValue());
    assertNull(recoveryCallbackCaptor.getValue());
    assertNull(retryStateCaptor.getValue());

    verify(restTemplate).getForObject(anyString(), any(Class.class));
    verify(restTemplate).postForObject(anyString(), any(TerminalPayload.class), (Class<String>) any(Class.class));

  }

}
