package co.enoobong.terminal.client.controller;

import co.enoobong.terminal.client.service.TerminalService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestClientResponseException;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(TerminalController.class)
public class TerminalControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private TerminalService terminalService;

  @Test
  public void processRequestShouldSuccessfullyProcessRequest() throws Exception {
    final String message = "success";
    given(terminalService.processRequest(null)).willReturn(message);

    mockMvc
            .perform(put("/v1/terminal/client"))
            .andExpect(status().isOk())
            .andExpect(content().string(message));
  }

  @Test
  public void processRequestShouldBe503WhenTerminalNotAvailable() throws Exception {
    final String message = "terminal not available";
    final int statusCode = 503;
    final String statusText = "service unavailable";
    given(terminalService.processRequest(null))
            .willThrow(
                    new RestClientResponseException(message, statusCode, statusText, null, null, null));

    mockMvc.perform(put("/v1/terminal/client")).andExpect(status().isServiceUnavailable());
  }
}
