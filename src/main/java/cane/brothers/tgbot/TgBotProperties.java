package cane.brothers.tgbot;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tgbot")
public record TgBotProperties(String token, OpenAI openai, HttpProxy proxy) {

    public record OpenAI(String token) {
    }

    public record HttpProxy(String hostname, int port, String username, String password) {
    }
}
