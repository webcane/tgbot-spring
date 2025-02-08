package cane.brothers.tgbot;

import cane.brothers.tgbot.telegram.TgBotCommand;
import cane.brothers.tgbot.telegram.TgBotSettings;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;


@Slf4j
@Component
@RequiredArgsConstructor
public class TgBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final static Map<CharSequence, CharSequence> ESCAPE_MAP = new HashMap<>() {
        {
            put("_", "\\_");
            put("*", "\\*");
            put("[", "\\[");
            put("]", "\\]");
            put("(", "\\(");
            put(")", "\\)");
            put("~", "\\~");
            put("`", "\\`");
            put(">", "\\>");
            put("<", "\\<");
            put("#", "\\#");
            put("+", "\\+");
            put("-", "\\-");
            put("=", "\\=");
            put("|", "\\|");
            put("{", "\\{");
            put("}", "\\}");
            put(".", "\\.");
            put("!", "\\!");
        }
    };
    private static final AggregateTranslator ESCAPE = new AggregateTranslator(
            new LookupTranslator(Collections.unmodifiableMap(ESCAPE_MAP))
    );

    private final TelegramClient telegramClient;
    private final TgBotProperties properties;
    private final ChatClient chatClient;
    private final TgBotSettings settings;

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
        // message
        if (update.hasMessage()) {
            Long chatId = update.getMessage().getChatId();
            var userMessage = update.getMessage().getText();
            Integer messageId = update.getMessage().getMessageId();

            // command
            if (update.getMessage().isCommand()) {
                String commandReply = getCommandReply(chatId, userMessage);
                sendReplyMessage(update, commandReply);
            }

            // gpt prompt
            else if (update.getMessage().hasText()) {
                sendReplyMessage(update, "already working on it");
                String answer = getGptAnswer(userMessage);

                // TODO send message builder customizers
                var msgBuilder = SendMessage.builder()
                        .chatId(chatId);
                if (settings.getUseMarkup(chatId)) {
                    msgBuilder.parseMode("MarkdownV2")
                            .text(StringEscapeUtils.builder(ESCAPE).escape(answer).toString());
                } else {
                    msgBuilder.text(Optional.ofNullable(answer).orElse("no answer"));
                }
                SendMessage reply = msgBuilder.build();
                logMessage(update, reply.getText());
                sendMessage(reply, () -> messageId);
            }
        }
    }

    protected String getCommandReply(Long chatId, String userMessage) {
        AtomicReference<String> reply = new AtomicReference<>();
        TgBotCommand.fromString(userMessage).ifPresentOrElse(command -> {
                    // update command setting
                    var useCommand = settings.updateChatSettings(chatId, command);
                    // return related command message
                    reply.set(command.getMessage(useCommand));
                },
                () -> {
                    var fallbackMessage = String.format("unknown command %s", userMessage);
                    log.warn(fallbackMessage);
                    reply.set(fallbackMessage);
                });
        return reply.get();
    }

    protected String getGptAnswer(String userMessage) {
        try {
            return chatClient.prompt()
                    .user(userMessage)
                    .call()
                    .content();
        } catch (Exception ex) {
            log.error(String.format("open-ai error: %s", ex.getMessage()), ex);
            return "Произошла ошибка при обработке запроса к gpt.";
        }
    }

    protected void sendReplyMessage(Update update, String reply) {
        Long chatId = update.getMessage().getChatId();
        if (settings.getUseReply(chatId)) {
            Integer messageId = update.getMessage().getMessageId();
            var replyMessage = SendMessage.builder().chatId(chatId)
                    .replyToMessageId(messageId)
                    .text(reply).build();

            logMessage(update, reply);
            sendMessage(replyMessage);
        }
    }
    protected void sendMessage(SendMessage reply) {
        sendMessage(reply, null);
    }

    protected void sendMessage(SendMessage sendMessage, Supplier<Integer> messageIdSupplier) {
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Can't send message to telegram", e);
            try {
                var msg = String.format("Произошла ошибка во время обрабоки. Попробуйте измений запрос.\n %s", e.getMessage());
                var fallbackMessageBuilder = SendMessage.builder()
                        .chatId(sendMessage.getChatId())
                        .text(msg);
                Optional.ofNullable(messageIdSupplier)
                        .ifPresent(a -> fallbackMessageBuilder.replyToMessageId(a.get()));
                SendMessage fallbackMessage = fallbackMessageBuilder.build();
                telegramClient.execute(fallbackMessage);
            } catch (TelegramApiException ex) {
                log.error("Can't send fallback message to telegram", ex);
            }
        }
    }

    private void logMessage(Update userMessage, String reply) {
        String user_first_name = userMessage.getMessage().getChat().getFirstName();
        String user_last_name = userMessage.getMessage().getChat().getLastName();
        String user_username = userMessage.getMessage().getChat().getUserName();
        long user_id = userMessage.getMessage().getChat().getId();
        String message_text = userMessage.getMessage().getText();
        log(user_first_name, user_last_name, Long.toString(user_id), user_username, message_text, reply);
    }

    private void log(String first_name, String last_name, String user_id, String user_username, String message_text, String bot_answer) {
        log.warn("----------------------------");
        log.info("User message from firstname={} lastname={} id={} username={}\n Text: \"{}\"\nTgBot answer:\n Text: \"{}\"", first_name, last_name, user_id, user_username, message_text, bot_answer);
        log.warn("----------------------------");
    }
}
