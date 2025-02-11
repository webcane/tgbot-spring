package cane.brothers.bot.telegram.settings;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
enum BooleanSetting implements CommandSetting {
    REPLY(Boolean.TRUE),
    MARKUP(Boolean.TRUE);

    private final Boolean defaultValue;

    public static BooleanSetting fromString(String command) {
        if (command.startsWith("/")) {
            var cmd = command.substring(1).toUpperCase();
            return BooleanSetting.valueOf(cmd);
        } else {
            throw new IllegalArgumentException("There is no relevant setting defined for the command \"%s\" "
                    .formatted(command));
        }
    }
}
