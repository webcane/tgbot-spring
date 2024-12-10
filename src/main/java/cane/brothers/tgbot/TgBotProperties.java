package cane.brothers.tgbot;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tgbot")
public record TgBotProperties(String token, HttpProxy proxy) {

    public record HttpProxy(String hostname, int port, String username, String password) {
    }
}
