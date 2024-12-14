package cane.brothers.tgbot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.translate.AggregateTranslator;
import org.apache.commons.text.translate.LookupTranslator;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.BotSession;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Slf4j
@Component
@RequiredArgsConstructor
public class TgBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final static Map<CharSequence, CharSequence> ESCAPE_MAP = new HashMap<>() {
        {
            put(".", "\\.");
            put("!", "\\!");
            put("(", "\\(");
            put(")", "\\)");
            put("#", "\\#");
            put("-", "\\-");
        }
    };
    private static final AggregateTranslator ESCAPE = new AggregateTranslator(
            new LookupTranslator(Collections.unmodifiableMap(ESCAPE_MAP))
    );

    private final TelegramClient telegramClient;
    private final TgBotProperties properties;
    private final ChatClient chatClient;
    private boolean useMarkup = false;

    @Override
    public String getBotToken() {
        return properties.token();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @AfterBotRegistration
    public void afterRegistration(BotSession botSession) {
        log.info("Registered bot running state is: {}", botSession.isRunning());
    }

    @Override
    public void consume(Update update) {
        // command
        if (update.hasMessage() && update.getMessage().isCommand()) {
            if (update.getMessage().getText().equals("/markup"))
                useMarkup = true;
            else if (update.getMessage().getText().equals("/start"))
                useMarkup = false;
        }

        // messages to gpt
        else if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String prompt = update.getMessage().getText();

            String answer;
            try {
                answer = chatClient.prompt()
                        .user(prompt)
                        .call()
                        .content();
            } catch (Exception ex) {
                log.error("open-ai error: " + ex.getMessage(), ex);
                answer = "Произошла ошибка при обработке запроса к open-ai.";
            }

            var msgBuilder = SendMessage.builder()
                    .chatId(chatId);
            if (useMarkup) {
                msgBuilder.parseMode("MarkdownV2")
                        .text(StringEscapeUtils.builder(ESCAPE).escape(answer).toString());
            } else {
                msgBuilder.text(Optional.ofNullable(answer).orElse("no answer"));
            }
            SendMessage reply = msgBuilder.build();
            logMessage(update, reply);
            sendMessage(reply);
        }
    }

    protected void sendMessage(SendMessage sendMessage) {
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Can't send message to telegram", e);
            try {
                SendMessage fallbackMessage = SendMessage.builder().chatId(sendMessage.getChatId())
                        .text("Произошла ошибка во время обрабоки. Попробуйте измений запрос.\n" + e.getMessage()).build();
                telegramClient.execute(fallbackMessage);
            } catch (TelegramApiException ex) {
                log.error("Can't send fallback message to telegram", ex);
            }
        }
    }

    private void logMessage(Update userMessage, SendMessage sendMessage) {
        String user_first_name = userMessage.getMessage().getChat().getFirstName();
        String user_last_name = userMessage.getMessage().getChat().getLastName();
        String user_username = userMessage.getMessage().getChat().getUserName();
        long user_id = userMessage.getMessage().getChat().getId();
        String message_text = userMessage.getMessage().getText();
        String reply = sendMessage.getText();
        log(user_first_name, user_last_name, Long.toString(user_id), user_username, message_text, reply);
    }

    private void log(String first_name, String last_name, String user_id, String user_username, String message_text, String bot_answer) {
        log.warn("----------------------------");
        log.info("User message from firstname={} lastname={} id={} username={}\n Text: \"{}\"\nTgBot answer:\n Text: \"{}\"", first_name, last_name, user_id, user_username, message_text, bot_answer);
        log.warn("----------------------------");
    }
}
