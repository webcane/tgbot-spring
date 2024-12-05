package cane.brothers.tgbot;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.longpolling.util.TelegramOkHttpClientFactory;
import java.net.InetSocketAddress;
import java.net.Proxy;

@Configuration
public class TgBotConfig {

    @Bean
    public OkHttpClient okClient(TgBotProperties properties) {
        return new TelegramOkHttpClientFactory.HttpProxyOkHttpClientCreator(
                () -> new Proxy(Proxy.Type.HTTP, new InetSocketAddress(properties.proxy().hostname(), properties.proxy().port())),
                () -> (route, response) -> {
                    String credential = Credentials.basic(properties.proxy().username(), properties.proxy().password());
                    return response
                            .request()
                            .newBuilder()
                            .header("Proxy-Authorization", credential)
                            .build();
                }
        ).get();
    }

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
