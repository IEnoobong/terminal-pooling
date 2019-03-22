package co.enoobong.terminal.server.service;

public interface TerminalService {

  String getAvailableTerminalId();

  void processRequest(String terminalId, int sequenceNo, long timestamp) throws Exception;
}
