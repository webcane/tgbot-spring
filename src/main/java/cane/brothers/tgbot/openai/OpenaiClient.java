package cane.brothers.tgbot.openai;

import cane.brothers.tgbot.message.IBotMessage;
import io.github.sashirestela.openai.SimpleOpenAI;
import io.github.sashirestela.openai.domain.chat.ChatMessage;
import io.github.sashirestela.openai.domain.chat.ChatRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenaiClient implements IBotMessage {

    private final SimpleOpenAI openAI;

    // Реализация запроса к OpenAI
    @Override
    public SendMessage getReply(Update update) {
        Long chatId = update.getMessage().getChatId();
        String prompt = update.getMessage().getText();

        // используемая модель
        // /v1/chat/completions
        //  models:
        //  - "gpt-3.5-turbo-instruct"
        //  - "babbage-002"
        //  - "davinci-002"
        String model = "gpt-4o-mini"; //"davinci-002"; // "babbage-002"; //"gpt-4o-mini"; // "gpt-3.5-turbo";

        var chatRequest = ChatRequest.builder()
                .model(model)
//                .message(SystemMessage.of("You are an expert in AI."))
                .message(ChatMessage.UserMessage.of(prompt))
                .temperature(0.0)
                .maxCompletionTokens(500)
                .build();

        String answer;
        try {
            log.debug("send prompt to open-ai api: \"{}\"", prompt);
            var futureChat = openAI.chatCompletions().create(chatRequest);
            var chatResponse = futureChat.join();
            answer = chatResponse.firstContent();
            log.debug("open-ai answer: \"{}\"", answer);

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
