package cane.brothers.tgbot;

import cane.brothers.tgbot.openai.OpenaiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.util.concurrent.atomic.AtomicReference;


@Slf4j
@Component
@RequiredArgsConstructor
public class TgBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;
    private final TgBotProperties properties;
    private final OpenaiClient openaiClient;

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
        // messages
        if (update.hasMessage() && update.getMessage().hasText()) {
            AtomicReference<SendMessage> reply = new AtomicReference<>();
            reply.set(openaiClient.getReply(update));
            logMessage(update, reply.get());
            sendMessage(reply.get());
        }
    }

    protected void sendMessage(SendMessage sendMessage) {
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Can't send message to telegram", e);
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
