package ru.job4j.grabber.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.*;

class HabrCareerDateTimeParserTest {

    @Test
    public void whenParseDateAndTimeThenReturnLocalDateTime() {
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        String dateAndTime = "2024-09-25T13:31:03+03:00";
        LocalDateTime expected = LocalDateTime.parse(dateAndTime, DateTimeFormatter.ISO_DATE_TIME);
        LocalDateTime result = parser.parse(dateAndTime);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void whenParseEmptyStringThenThrowDateTimeParseException() {
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        assertThatThrownBy(() -> parser.parse("")).isInstanceOf(DateTimeParseException.class);
    }

    @Test
    public void whenParseStringNotContainsCorrectFormatThenThrowDateTimeParseException() {
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        String dateAndTime = "20240925T13:31:03+03:00";
        assertThatThrownBy(() -> parser.parse(dateAndTime)).isInstanceOf(DateTimeParseException.class);
    }
}