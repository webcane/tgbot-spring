package cane.brothers.tgbot.telegram;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class TgBotCommandTest {

    @Test
    void test_toString() {
        Assertions.assertThat(TgBotCommand.MARKUP.toString())
                .isEqualTo("/markup");
    }

    @Test
    void test_toString_equals() {
        Assertions.assertThat("/markup".equals(TgBotCommand.MARKUP.toString()))
                .isTrue();
    }

    @Test
    void test_name() {
        Assertions.assertThat(TgBotCommand.MARKUP.name())
                .isEqualTo("MARKUP");
    }
}