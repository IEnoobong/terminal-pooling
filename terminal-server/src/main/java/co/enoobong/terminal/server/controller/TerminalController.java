package co.enoobong.terminal.server.controller;

import co.enoobong.terminal.common.model.request.TerminalPayload;
import co.enoobong.terminal.common.model.response.TerminalResponse;
import co.enoobong.terminal.server.service.TerminalService;
import javax.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "v1/terminal/server", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class TerminalController {

  private final TerminalService terminalService;

  public TerminalController(TerminalService terminalService) {
    this.terminalService = terminalService;
  }

  @GetMapping("availableTerminalId")
  public ResponseEntity<TerminalResponse> getAvailableTerminalId() {
    final String terminalId = terminalService.getAvailableTerminalId();
    return ResponseEntity.ok(new TerminalResponse(terminalId));
  }

  @PostMapping
  public ResponseEntity<String> processTerminalRequest(@RequestBody @Valid TerminalPayload payload) throws Exception {
    terminalService.processRequest(payload.getTerminalId(), payload.getSequenceNo(), payload.getTimestamp());
    return ResponseEntity.ok().build();
  }
}
