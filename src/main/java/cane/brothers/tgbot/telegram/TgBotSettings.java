package cane.brothers.tgbot.telegram;

import java.util.HashMap;
import java.util.Map;

public class TgBotSettings {

    private static final Map<Long, Map<TgBotCommand, Boolean>> settings = new HashMap<>();

    public Boolean getUseMarkup(Long chatId) {
        return getChatSettings(chatId, TgBotCommand.MARKUP);
    }

    public Boolean getUseReply(Long chatId) {
        return getChatSettings(chatId, TgBotCommand.REPLY);
    }

    public Boolean updateChatSettings(Long chatId, TgBotCommand command) {
        return settings.computeIfAbsent(chatId, chat -> new HashMap<>())
                .merge(command, !command.getDefaultValue(), (oldValue, newValue) -> !oldValue);
    }

    public Boolean getChatSettings(Long chatId, TgBotCommand command) {
        return settings.computeIfAbsent(chatId, chat -> new HashMap<>())
                .computeIfAbsent(command, cmd -> cmd.getDefaultValue());
    }


}
