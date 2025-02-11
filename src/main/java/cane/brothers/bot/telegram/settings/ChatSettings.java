package cane.brothers.bot.telegram.settings;

public interface ChatSettings {

    Boolean getUseMarkup(Long chatId);

    Boolean getUseReply(Long chatId);

    Boolean updateCommand(Long chatId, String command);

}
