package cane.brothers.tgbot.telegram;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TgBotSettingsTest {

    private TgBotSettings settings;
    private final Long chatId = 123L;
    private final Long chatId2 = 321L;

    @BeforeEach
    public void setUp() {
        settings = new TgBotSettings();
    }

    @Nested
    class GetChatSettingsTest {

        @ParameterizedTest
        @CsvSource(value = {
                "/markup,true",
                "/reply,true",
                "/start,false"
        })
        void test_getUseMarkup_defaultValue(String input, String expected) {
            var command = TgBotCommand.fromString(input);
            var result = settings.getChatSettings(chatId2, command.get());
            Assertions.assertThat(result)
                    .isEqualTo(Boolean.valueOf(expected));
        }
    }

    @Nested
    class UpdateSettingsTest {

        @ParameterizedTest
        @CsvSource(value = {
                "/markup,false",
                "/reply,false",
                "/start,true"
        })
        void test_updateSettings_firstTime(String input, String expected) {
            var command = TgBotCommand.fromString(input);
            var result = settings.updateChatSettings(chatId, command.get());
            Assertions.assertThat(result)
                    .isEqualTo(Boolean.valueOf(expected));
        }

        @Test
        void test_updateSettings_twice() {
            var result = settings.updateChatSettings(chatId, TgBotCommand.MARKUP);
            result = settings.updateChatSettings(chatId, TgBotCommand.MARKUP);
            Assertions.assertThat(result)
                    .isEqualTo(Boolean.TRUE);
        }
    }

}