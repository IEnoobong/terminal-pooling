package co.enoobong.terminal.common.model.response;

public class TerminalResponse {

  private String terminalId;

  public TerminalResponse() {
    // Required Empty Constructor for (de)serialization
  }

  public TerminalResponse(String terminalId) {
    this.terminalId = terminalId;
  }

  public String getTerminalId() {
    return terminalId;
  }
}
