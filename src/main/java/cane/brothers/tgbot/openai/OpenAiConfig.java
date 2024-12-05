package cane.brothers.tgbot.openai;

import cane.brothers.tgbot.TgBotProperties;
import io.github.sashirestela.openai.SimpleOpenAI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAiConfig {

    @Bean
    SimpleOpenAI openAI(TgBotProperties properties) {
        return SimpleOpenAI.builder()
                .apiKey(properties.openai().token())
                .build();
    }
}
