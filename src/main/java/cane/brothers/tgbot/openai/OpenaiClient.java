package cane.brothers.tgbot.openai;

import cane.brothers.tgbot.message.IBotMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenaiClient implements IBotMessage {

    private final ChatClient chatClient;

    @Override
    public SendMessage getReply(Update update) {
        Long chatId = update.getMessage().getChatId();
        String prompt = update.getMessage().getText();

        String answer = "";
        try {
            answer = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception ex) {
            log.error("open-ai error: " + ex.getMessage(), ex);
            answer = "Произошла ошибка при обработке запроса к open-ai.";
        }

        return SendMessage.builder()
                .chatId(chatId)
                .text(answer)
                .build();
    }
}
