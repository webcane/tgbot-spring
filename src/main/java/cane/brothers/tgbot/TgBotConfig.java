package cane.brothers.tgbot;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Configuration
public class TgBotConfig {

    @Bean
    public TelegramClient telegramClient(OkHttpClient okClient, TgBotProperties properties) {
        return new OkHttpTelegramClient(okClient, properties.token());
    }

    @Bean
    public TelegramBotsLongPollingApplication telegramBotsApplication(ObjectMapper objectMapper,
                                                                      OkHttpClient okClient) {
        return new TelegramBotsLongPollingApplication(() -> objectMapper, () -> okClient);
    }
}
