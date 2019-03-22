package co.enoobong.terminal.common.model.request;

import javax.validation.constraints.NotBlank;

public class TerminalPayload {

  @NotBlank
  private String terminalId;
  private int sequenceNo;
  private long timestamp;

  public TerminalPayload() {
  }

  public TerminalPayload(String terminalId, int sequenceNo, long timestamp) {
    this.terminalId = terminalId;
    this.sequenceNo = sequenceNo;
    this.timestamp = timestamp;
  }

  public String getTerminalId() {
    return terminalId;
  }

  public int getSequenceNo() {
    return sequenceNo;
  }

  public long getTimestamp() {
    return timestamp;
  }
}
