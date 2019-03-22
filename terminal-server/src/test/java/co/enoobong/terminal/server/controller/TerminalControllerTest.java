package co.enoobong.terminal.server.controller;

import co.enoobong.terminal.common.model.request.TerminalPayload;
import co.enoobong.terminal.server.exception.InvalidRequestException;
import co.enoobong.terminal.server.exception.TerminalNotAvailableException;
import co.enoobong.terminal.server.service.TerminalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static co.enoobong.terminal.server.util.TestUtils.BASE_TERMINAL_PATH;
import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(TerminalController.class)
public class TerminalControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private TerminalService terminalService;

  @Test
  public void getAvailableTerminalIdShouldGetIt() throws Exception {
    final String terminalId = "1001";
    given(terminalService.getAvailableTerminalId()).willReturn(terminalId);

    mockMvc
            .perform(get(BASE_TERMINAL_PATH + "/availableTerminalId"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.terminalId", is(terminalId)));
  }

  @Test
  public void getAvailableTerminalIdWhenNoAvailableShouldBe503() throws Exception {
    final String message = "terminal not available";
    given(terminalService.getAvailableTerminalId()).willThrow(new TerminalNotAvailableException(message));

    mockMvc
            .perform(get(BASE_TERMINAL_PATH + "/availableTerminalId"))
            .andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.message", is(message)));
  }

  @Test
  public void processRequestShouldProcessRequest() throws Exception {
    final TerminalPayload terminalPayload = new TerminalPayload("1001", 2, System.currentTimeMillis());
    willDoNothing().given(terminalService).processRequest(terminalPayload.getTerminalId(),
            terminalPayload.getSequenceNo(), terminalPayload.getTimestamp());

    mockMvc.perform(post(BASE_TERMINAL_PATH)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(objectMapper.writeValueAsString(terminalPayload)))
            .andExpect(status().isOk());
  }

  @Test
  public void processRequestWhenInvalidTerminalIdOrSequenceNumberShouldBe400() throws Exception {
    final TerminalPayload terminalPayload = new TerminalPayload("100001", 100, System.currentTimeMillis());
    final String message = "not valid request";
    willThrow(new InvalidRequestException(message)).given(terminalService).processRequest(terminalPayload.getTerminalId(),
            terminalPayload.getSequenceNo(), terminalPayload.getTimestamp());

    mockMvc.perform(post(BASE_TERMINAL_PATH)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(objectMapper.writeValueAsString(terminalPayload)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", is(message)));
  }
}
