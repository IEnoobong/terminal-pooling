package co.enoobong.terminal.server.service.impl;

import co.enoobong.terminal.common.config.TerminalConfig;
import co.enoobong.terminal.server.exception.InvalidRequestException;
import co.enoobong.terminal.server.exception.TerminalNotAvailableException;
import co.enoobong.terminal.server.service.TerminalService;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InMemoryTerminalService implements TerminalService {

  private static final Logger log = LoggerFactory.getLogger(InMemoryTerminalService.class);

  private final Map<String, Boolean> terminalIdToAvailability = new HashMap<>();

  private final TerminalConfig terminalConfig;
  private final String[] terminalIds;

  public InMemoryTerminalService(TerminalConfig terminalConfig, @Value("${available.terminals}") String[] terminalIds) {
    this.terminalConfig = terminalConfig;
    this.terminalIds = terminalIds;
  }

  private static boolean between(int i, int minValueInclusive, int maxValueExclusive) {
    return (i >= minValueInclusive && i < maxValueExclusive);
  }

  @PostConstruct
  void loadData() {
    for (String terminalId : terminalIds) {
      terminalIdToAvailability.put(terminalId, false);
    }
  }

  @Override
  public String getAvailableTerminalId() {
    synchronized (terminalIdToAvailability) {
      final String terminalId = terminalIdToAvailability.entrySet().stream()
              .filter(entry -> !entry.getValue())
              .findAny()
              .map(Map.Entry::getKey)
              .orElseThrow(() -> new TerminalNotAvailableException("terminal not available"));
      terminalIdToAvailability.put(terminalId, true);
      return terminalId;
    }
  }

  @Override
  public void processRequest(String terminalId, int sequenceNo, long timestamp) throws Exception {
    log.info("Terminal Payload: terminalId={} sequence number={} timestamp={}", terminalId, sequenceNo, timestamp);
    if (isNotValidRequest(sequenceNo, terminalId)) {
      throw new InvalidRequestException(String.format("sequenceNo(%d) is either not in range or terminal(%s) not locked for use", sequenceNo, terminalId));
    }
    try {
      TimeUnit.SECONDS.sleep(1);
    } finally {
      terminalIdToAvailability.put(terminalId, false);
    }
  }

  private boolean isNotValidRequest(int sequenceNo, String terminalId) {
    boolean isLocked = existByTerminalIdAndIsLocked(terminalId);
    return !between(sequenceNo, terminalConfig.getStart(), terminalConfig.getEnd()) || !isLocked;
  }

  private boolean existByTerminalIdAndIsLocked(String terminalId) {
    final Boolean isLocked = terminalIdToAvailability.get(terminalId);
    return isLocked != null && isLocked;
  }

  @PreDestroy
  void preDestroy() {
    terminalIdToAvailability.clear();
  }
}
