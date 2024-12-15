package cane.brothers.tgbot.telegram;

public interface ICommandMessage {

    String getMessage(Boolean useCommand);

    Boolean getDefaultValue();
}
