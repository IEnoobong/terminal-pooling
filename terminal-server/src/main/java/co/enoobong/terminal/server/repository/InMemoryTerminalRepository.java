package co.enoobong.terminal.server.repository;

import co.enoobong.terminal.server.exception.TerminalNotAvailableException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryTerminalRepository {

  private final Map<String, Long> terminalIdToAvailability = new HashMap<>();
  private ScheduledExecutorService scheduledExecutorService;

  @Value("${available.terminals}")
  private String[] terminalIds;

  @Value("${terminal.available.period.seconds}")
  private long terminalAvailabilityPeriod;

  private boolean isInitialized;

  @PostConstruct
  void loadData() {
    for (String terminalId : terminalIds) {
      terminalIdToAvailability.put(terminalId, Long.MIN_VALUE);
    }
  }

  public String getTerminalId() {
    synchronized (terminalIdToAvailability) {
      final String availableTerminal = terminalIdToAvailability.entrySet().stream()
              .filter(entry -> entry.getValue() < 0)
              .findAny()
              .map(Map.Entry::getKey)
              .orElseThrow(() -> new TerminalNotAvailableException("terminal not available"));
      terminalIdToAvailability.put(availableTerminal, getAvailableTimeInNanoSecs() + System.nanoTime());
      initializeCleanUp();
      return availableTerminal;
    }
  }

  private void initializeCleanUp() {
    if (!isInitialized) {
      scheduledExecutorService = Executors.newScheduledThreadPool(1);
      scheduledExecutorService.scheduleAtFixedRate(this::reactivateUnusedTerminalsAfterPeriod, terminalAvailabilityPeriod, terminalAvailabilityPeriod, TimeUnit.SECONDS);
      isInitialized = true;
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

  public boolean isTerminalValid(String terminalId) {
    final Long aLong = terminalIdToAvailability.get(terminalId);
    if (aLong != null) {
      return (aLong - System.nanoTime()) < getAvailableTimeInNanoSecs();
    } else {
      return false;
    }
  }

  public void makeTerminalAvailable(String terminalId) {
    terminalIdToAvailability.put(terminalId, Long.MIN_VALUE);
  }

  @PreDestroy
  void preDestroy() {
    terminalIdToAvailability.clear();
    if (scheduledExecutorService != null) {
      scheduledExecutorService.shutdownNow();
    }
  }
}
