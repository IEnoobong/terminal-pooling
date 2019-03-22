package co.enoobong.terminal.client.controller;

import co.enoobong.terminal.client.service.TerminalService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "v1/terminal/client", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class TerminalController {

  private final TerminalService terminalService;

  @Autowired
  public TerminalController(TerminalService terminalService) {
    this.terminalService = terminalService;
  }

  @PutMapping
  public ResponseEntity<String> processRequest(@RequestBody(required = false) Map<String, Object> payload) {
    final String response = terminalService.processRequest(payload);
    return ResponseEntity.ok(response);
  }
}
