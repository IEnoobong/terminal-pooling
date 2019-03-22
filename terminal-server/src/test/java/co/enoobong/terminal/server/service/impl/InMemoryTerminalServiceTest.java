package co.enoobong.terminal.server.service.impl;

import co.enoobong.terminal.common.config.TerminalConfig;
import co.enoobong.terminal.server.exception.InvalidRequestException;
import co.enoobong.terminal.server.exception.TerminalNotAvailableException;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class InMemoryTerminalServiceTest {

  @Rule
  public final ExpectedException expectedException = ExpectedException.none();
  private final String[] terminalIds = {"1001", "1002", "1003", "1004"};
  private TerminalConfig terminalConfig = mock(TerminalConfig.class);
  private InMemoryTerminalService terminalService;

  @Before
  public void setup() {
    given(terminalConfig.getStart()).willReturn(0);
    given(terminalConfig.getEnd()).willReturn(8);

    terminalService = new InMemoryTerminalService(terminalConfig, terminalIds);
    terminalService.loadData();
  }

  @Test
  public void shouldGetAvailableTerminalWhenExists() {
    final String terminalId = terminalService.getAvailableTerminalId();
    assertThat(Arrays.asList(terminalIds), hasItem(terminalId));
  }

  @Test
  public void shouldThrowTerminalNotAvailableExceptionWhenNoTerminalAvailable() {
    terminalService = new InMemoryTerminalService(terminalConfig, null);

    expectedException.expect(TerminalNotAvailableException.class);
    expectedException.expectMessage(containsString("terminal not available"));

    terminalService.getAvailableTerminalId();
  }

  @Test
  public void shouldProcessValidRequest() throws Exception {
    final String terminalId = terminalService.getAvailableTerminalId();
    final int sequenceNo = 2;
    final long timestamp = System.currentTimeMillis();

    terminalService.processRequest(terminalId, sequenceNo, timestamp);

    verify(terminalConfig).getStart();
    verify(terminalConfig).getEnd();
  }

  @Test
  public void whenProcessRequestShouldThrowInvalidRequestExceptionWhenSeqNoOrIdInvalid() throws Exception {
    final String terminalId = "hahsnd";
    final int sequenceNo = 90;
    final long timestamp = System.currentTimeMillis();

    expectedException.expect(InvalidRequestException.class);
    expectedException.expectMessage(containsString("is either not in range or "));

    terminalService.processRequest(terminalId, sequenceNo, timestamp);

    verify(terminalConfig).getStart();
    verify(terminalConfig).getEnd();
  }

}
