package co.enoobong.terminal.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:terminal-server.properties")
public class TerminalServerConfig {
}
