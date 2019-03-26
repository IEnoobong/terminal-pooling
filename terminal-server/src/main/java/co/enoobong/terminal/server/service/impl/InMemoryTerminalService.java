package co.enoobong.terminal.server.service.impl;

import co.enoobong.terminal.common.config.TerminalConfig;
import co.enoobong.terminal.server.exception.InvalidRequestException;
import co.enoobong.terminal.server.exception.TerminalNotAvailableException;
import co.enoobong.terminal.server.service.TerminalService;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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

  private final Map<String, Long> terminalIdToAvailability = new HashMap<>();
  private ScheduledExecutorService scheduledExecutorService;

  private final TerminalConfig terminalConfig;
  private final String[] terminalIds;

  @Value("${terminal.processing.time.seconds}")
  private long terminalProcessingTime;

  @Value("${terminal.available.period.seconds}")
  private long terminalAvailabilityPeriod;

  public InMemoryTerminalService(TerminalConfig terminalConfig, @Value("${available.terminals}") String[] terminalIds) {
    this.terminalConfig = terminalConfig;
    this.terminalIds = terminalIds;
  }

  private boolean isInitialized;

  private static boolean between(int value, int minValueInclusive, int maxValueExclusive) {
    return (value >= minValueInclusive && value < maxValueExclusive);
  }

  @PostConstruct
  void loadData() {
    for (String terminalId : terminalIds) {
      terminalIdToAvailability.put(terminalId, Long.MIN_VALUE);
    }
  }

  private void reactivateUnusedTerminalsAfterPeriod() {
    synchronized (terminalIdToAvailability) {
      terminalIdToAvailability.entrySet().stream()
              .filter(entry -> {
                final long timeElapsed = System.nanoTime() - entry.getValue();
                return timeElapsed > 0;
              })
              .forEach(entry -> terminalIdToAvailability.put(entry.getKey(), Long.MIN_VALUE));
    }
  }

  private long getAvailableTimeInNanoSecs() {
    return TimeUnit.SECONDS.toNanos(terminalAvailabilityPeriod);
  }

  @Override
  public String getAvailableTerminalId() {
    synchronized (terminalIdToAvailability) {
      final String availableTerminal = terminalIdToAvailability.entrySet().stream()
              .filter(entry -> entry.getValue() < 0)
              .findAny()
              .map(Map.Entry::getKey)
              .orElseThrow(() -> new TerminalNotAvailableException("terminal not available"));
      terminalIdToAvailability.put(availableTerminal, getAvailableTimeInNanoSecs() + System.nanoTime());
      if (!isInitialized) {
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(this::reactivateUnusedTerminalsAfterPeriod, terminalAvailabilityPeriod, terminalAvailabilityPeriod, TimeUnit.SECONDS);
        isInitialized = true;
      }
      return availableTerminal;
    }
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
      terminalIdToAvailability.put(terminalId, Long.MIN_VALUE);
    }
  }

  private boolean isNotValidRequest(int sequenceNo, String terminalId) {
    return !between(sequenceNo, terminalConfig.getStart(), terminalConfig.getEnd()) || !isTerminalValid(terminalId);
  }

  private boolean isTerminalValid(String terminalId) {
    final Long aLong = terminalIdToAvailability.get(terminalId);
    if (aLong != null) {
      return (aLong - System.nanoTime()) < getAvailableTimeInNanoSecs();
    } else {
      return false;
    }
  }

  @PreDestroy
  void preDestroy() {
    terminalIdToAvailability.clear();
    if (scheduledExecutorService != null) {
      scheduledExecutorService.shutdownNow();
    }
  }
}
