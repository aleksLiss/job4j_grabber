package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;

import java.io.IOException;
import java.util.StringJoiner;

public class HabrCareerParse {
    private static final int COUNT_OF_PAGE = 5;
    private static final String SOURCE_LINK = "https://career.habr.com";
    public static final String PREFIX = "/vacancies?page=";
    public static final String SUFFIX = "&q=Java%20developer&type=all";
    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static void main(String[] args) throws IOException {
        parsePages();
        retrieveDescription("https://career.habr.com/vacancies/1000151189");
    }

    private static void parsePages() throws IOException {
        int pageNumber = 1;
        while (pageNumber != COUNT_OF_PAGE + 1) {
            String fullLink = "%s%s%d%s".formatted(SOURCE_LINK, PREFIX, pageNumber, SUFFIX);
            Connection connection = Jsoup.connect(fullLink);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();
                String date = row.firstChild().childNode(0).attr("datetime");
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                System.out.printf("%s: %s %s%n", date, vacancyName, link);
            });
            pageNumber--;
        }
    }

    private static String retrieveDescription(String link) throws IOException {
        StringJoiner derscription = new StringJoiner("\n");
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Elements rows = document.select(".vacancy-description__text");
        rows.forEach(row -> {
            Elements elements = row.children();
            elements.forEach(i -> {
                Elements elements1 = i.children();
                elements1.forEach(j -> derscription.add(j.text()));
            });
        });
        return derscription.toString();
    }
}