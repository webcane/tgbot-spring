package cane.brothers.tgbot;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.web.client.RestClientBuilderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.telegram.telegrambots.longpolling.util.TelegramOkHttpClientFactory;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Configuration
public class HttpClientConfig {

//    @Bean
//    HttpClient httpClient(TgBotProperties properties) {
//        return HttpClient.newBuilder()
//                .proxy(ProxySelector.of(new InetSocketAddress(properties.proxy().hostname(), properties.proxy().port())))
//                .build();
//    }

//    @Bean
//    RestClientCustomizer proxyRestClientCustomizer(TgBotProperties properties) {
//        String credential = Credentials.basic(properties.proxy().username(), properties.proxy().password());
//        return restClientBuilder -> restClientBuilder.defaultHeader(HttpHeaders.PROXY_AUTHORIZATION, credential);
//    }

    @Bean
    public OkHttpClient okClient(TgBotProperties properties) {
        return new TelegramOkHttpClientFactory.HttpProxyOkHttpClientCreator(
                () -> new Proxy(Proxy.Type.HTTP, new InetSocketAddress(properties.proxy().hostname(), properties.proxy().port())),
                () -> (route, response) -> {
                    String credential = Credentials.basic(properties.proxy().username(), properties.proxy().password());
                    return response
                            .request()
                            .newBuilder()
                            .header(HttpHeaders.PROXY_AUTHORIZATION, credential)
                            .build();
                }
        ).get();
    }

    @Bean
    OkHttp3ClientHttpRequestFactory okHttp3ClientHttpRequestFactory(OkHttpClient okHttpClient) {
        return new OkHttp3ClientHttpRequestFactory(okHttpClient);
    }

    @Bean
    RestClient.Builder restClientBuilder(RestClientBuilderConfigurer restClientBuilderConfigurer,
                                         OkHttp3ClientHttpRequestFactory okHttp3ClientHttpRequestFactory) {
        RestClient.Builder builder = RestClient.builder()
                .requestFactory(okHttp3ClientHttpRequestFactory);
        return restClientBuilderConfigurer.configure(builder);
    }
}
