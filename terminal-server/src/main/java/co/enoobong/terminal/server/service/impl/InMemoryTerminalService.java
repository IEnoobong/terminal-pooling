package co.enoobong.terminal.server.service.impl;

import co.enoobong.terminal.common.config.TerminalConfig;
import co.enoobong.terminal.server.exception.InvalidRequestException;
import co.enoobong.terminal.server.repository.InMemoryTerminalRepository;
import co.enoobong.terminal.server.service.TerminalService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class InMemoryTerminalService implements TerminalService {

  private static final Logger log = LoggerFactory.getLogger(InMemoryTerminalService.class);

  private final TerminalConfig terminalConfig;
  private final InMemoryTerminalRepository terminalRepository;

  @Value("${terminal.processing.time.seconds}")
  private long terminalProcessingTime;

  @Autowired
  public InMemoryTerminalService(TerminalConfig terminalConfig, InMemoryTerminalRepository terminalRepository) {
    this.terminalConfig = terminalConfig;
    this.terminalRepository = terminalRepository;
  }

  private static boolean between(int value, int minValueInclusive, int maxValueExclusive) {
    return (value >= minValueInclusive && value < maxValueExclusive);
  }

  @Override
  public String getAvailableTerminalId() {
    return terminalRepository.getTerminalId();
  }

  @Override
  public void processRequest(String terminalId, int sequenceNo, long timestamp) throws Exception {
    log.info("Terminal Payload: terminalId={} sequence number={} timestamp={}", terminalId, sequenceNo, timestamp);
    if (isNotValidRequest(sequenceNo, terminalId)) {
      throw new InvalidRequestException(String.format("sequenceNo(%d) is either not in range or terminal(%s) not locked for use", sequenceNo, terminalId));
    }
    try {
      TimeUnit.SECONDS.sleep(terminalProcessingTime);
    } finally {
      terminalRepository.makeTerminalAvailable(terminalId);
    }
  }

  private boolean isNotValidRequest(int sequenceNo, String terminalId) {
    return !between(sequenceNo, terminalConfig.getStart(), terminalConfig.getEnd()) || !terminalRepository.isTerminalValid(terminalId);
  }
}
