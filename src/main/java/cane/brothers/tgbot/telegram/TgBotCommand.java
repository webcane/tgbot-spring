package cane.brothers.tgbot.telegram;

import java.util.Arrays;
import java.util.Optional;

public enum TgBotCommand implements ICommandMessage {
    START,
    MARKUP {
        @Override
        public Boolean getDefaultValue() {
            return Boolean.TRUE;
        }
    },
    REPLY {
        @Override
        public Boolean getDefaultValue() {
            return Boolean.TRUE;
        }
    };

    public static Optional<TgBotCommand> fromString(String str) {
        if (str == null || str.length() < 2) {
            return Optional.empty();
        }

        return Arrays.stream(TgBotCommand.values())
                .filter(command -> command.toString().equals(str))
                .findFirst();
    }

    @Override
    public String toString() {
        return String.format("/%s", name().toLowerCase());
    }

    @Override
    public String getMessage(Boolean useCommand) {
        return String.format("%s is %s", this, useCommand ? "active" : "inactive");
    }

    @Override
    public Boolean getDefaultValue() {
        return Boolean.FALSE;
    }


}
