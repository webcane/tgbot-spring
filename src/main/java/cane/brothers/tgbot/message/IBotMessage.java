package cane.brothers.tgbot.message;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface IBotMessage {
    SendMessage getReply(Update update);
}
