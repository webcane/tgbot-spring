package cane.brothers.bot.telegram.commands;

import cane.brothers.bot.telegram.settings.ChatSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Optional;

@Slf4j
@Component("/gpt")
@RequiredArgsConstructor
class ReplyGptCommand implements ChatCommand<Message>, Utils {

    private final ChatClient chatClient;
    private final TelegramClient telegramClient;
    private final ChatSettings botSettings;

    @Override
    public void execute(Message data) throws TelegramApiException {
        Long chatId = data.getChatId();
        Integer messageId = data.getMessageId();
        logUserMessage(data);

        String answer = getGptAnswer(data.getText());

        var msgBuilder = SendMessage.builder().chatId(chatId);
        if (botSettings.getUseReply(chatId)) {
            // send reply message
            msgBuilder.replyToMessageId(messageId);
        }
        if (botSettings.getUseMarkup(chatId)) {
            msgBuilder.parseMode(ParseMode.MARKDOWNV2)
                    .text(escape(answer));
        } else {
            msgBuilder.text(Optional.ofNullable(answer).orElse("no clue"));
        }

        var reply = msgBuilder.build();
        telegramClient.execute(reply);
    }

    String getGptAnswer(String userMessage) {
        try {
            return chatClient.prompt()
                    .user(userMessage)
                    .call()
                    .content();
        } catch (Exception ex) {
            log.error("open-ai error", ex);
            return "An error occurred while processing the request to the open-ai service.";
        }
    }

    private void logUserMessage(Message data) {
        String user_first_name = data.getChat().getFirstName();
        String user_last_name = data.getChat().getLastName();
        String user_username = data.getChat().getUserName();
        long user_id = data.getChat().getId();
        log.info("username={} firstname={} lastname={} id={}", user_username, user_first_name, user_last_name, user_id);
    }
}
