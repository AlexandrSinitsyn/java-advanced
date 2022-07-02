package info.kgeorgiy.ja.sinitsyn.i18n;

import info.kgeorgiy.ja.sinitsyn.i18n.stats.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@RunWith(JUnit4.class)
public final class TextStatisticsTest {

    private static String TEST_FOLDER;//"../examples/";

    static {
        try {
            TEST_FOLDER = Paths.get(TextStatistics.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                    .resolve(Paths.get("../".repeat(3) + "solutions/examples/"))
                    .toString() + File.separator;
        } catch (final URISyntaxException e) {
            Assert.fail("Can not found folder-path");
        }
    }

    private static final Locale RUSSIAN_LOCALE = new Locale("ru", "RU");

    private void test(final AbstractStatistic<?> statistic, final List<Long> expectedAnswers) {
        final BiConsumer<Long, Long> test = (expected, actual) ->
                Assert.assertEquals("Expected equal count", expected, actual);

        final String[] requests = {
                "",
                "Hello",
                "Hello, World",
                "Hello, World!",
                "123",
                "100,00 ₽",
                "19 мая 2022 г.",
                "Аргументы командной строки: локаль текста, локаль вывода, файл с текстом, файл отчета.",
"""
Статистика по суммам денег
    Число сумм: 3 (3 различных).
    Минимальная сумма: 100,00 ₽.
    Максимальная сумма: 345,67 ₽.
    Средняя сумма: 222,83 ₽.
Статистика по датам
    Число дат: 3 (3 различных).
    Минимальная дата: 19 мая 2022 г..
    Максимальная дата: 2 июн. 2022 г..
    Средняя дата: 26 мая 2022 г..
""",
        };

        boolean failed = false;
        for (int i = 0; i < expectedAnswers.size(); i++) {
            statistic.reset();
            statistic.process(requests[i]);

            try {
                test.accept(expectedAnswers.get(i), statistic.getOccurrences());
            } catch (final AssertionError e) {
                failed = true;
                System.err.println("For test:\n\t" + requests[i] + "\n\twas: " + e.getMessage());
            }
        }

        if (failed) {
            Assert.fail();
        }
    }

    @Test
    public void sentenceStatisticsTest() {
        test(new SentenceStatistic(), Stream.of(0, 1, 1, 1, 1, 1, 1, 1, 8).mapToLong(Long::valueOf).boxed().toList());
    }

    @Test
    public void wordsStatisticsTest() {
        test(new WordStatistic(), Stream.of(0, 1, 2, 2, 0, 0, 2, 12, 31).mapToLong(Long::valueOf).boxed().toList());
    }

    @Test
    public void numbersStatisticsTest() {
        test(new NumberStatistic(), Stream.of(0, 0, 0, 0, 1, 0, 0, 0, 4).mapToLong(Long::valueOf).boxed().toList());
    }

    @Test
    public void moneyStatisticsTest() {
        test(new MoneyStatistic(), Stream.of(0, 0, 0, 0, 0, 1, 0, 0, 3).mapToLong(Long::valueOf).boxed().toList());
    }

    @Test
    public void datesStatisticsTest() {
        test(new DateStatistic(), Stream.of(0, 0, 0, 0, 0, 0, 1, 0, 3).mapToLong(Long::valueOf).boxed().toList());
    }

    @Test
    public void updateTest() {
        final String inputFilePath = TEST_FOLDER + "updatingFile.txt";

        final Function<String, Boolean> write = text -> {
            try (final var writer = new BufferedWriter(Files.newBufferedWriter(Paths.get(inputFilePath), StandardCharsets.UTF_8))) {
                writer.write(text);
            } catch (final IOException e) {
                Assert.fail("Rerun this test, please. Test generation went wrong");
                return false;
            }

            return true;
        };

        final TextStatistics statistics = TextStatistics.statisticsOf(RUSSIAN_LOCALE, Locale.ENGLISH, inputFilePath, "unknown.file");

        write.apply("Hello, world!");

        statistics.updateStatistics();

        Assert.assertEquals("Expected 2 words", 2, statistics.wordStatistic().getOccurrences());
        Assert.assertEquals("Expected 0 numbers", 0, statistics.numberStatistic().getOccurrences());

        write.apply("123 321");

        statistics.updateStatistics();

        Assert.assertEquals("Expected 0 words", 0, statistics.wordStatistic().getOccurrences());
        Assert.assertEquals("Expected 2 numbers", 2, statistics.numberStatistic().getOccurrences());
    }

    @Test
    public void in_out_test() {
        final String inputFilePath = TEST_FOLDER + "base_test.txt";
        final String outputFilePath = TEST_FOLDER + "base_test_result.txt";
        final TextStatistics statistics =
                TextStatistics.statisticsOf(RUSSIAN_LOCALE, Locale.US, inputFilePath, outputFilePath);

        statistics.updateStatistics();

        statistics.writeStatistics();

        try (final var reader = new BufferedReader(Files.newBufferedReader(Paths.get(outputFilePath), StandardCharsets.UTF_8))) {
            String result;
            while ((result = reader.readLine()) != null) {
                if (result.equals("\tCount of sentences: 43,0 (43,0 different).")) {
                    return;
                }
            }
            Assert.fail("Expected string, but found <none>");
        } catch (final IOException e) {
            Assert.fail("Rerun this test, please. Test generation went wrong");
        }
    }

    @Test
    public void in_out_test2() {
        final String inputFilePath = TEST_FOLDER + "base_test.txt";
        final String outputFilePath = TEST_FOLDER + "base_test_result.txt";
        final TextStatistics statistics =
                TextStatistics.statisticsOf(RUSSIAN_LOCALE, Locale.US, inputFilePath, outputFilePath);

        statistics.updateStatistics();

        Assert.assertArrayEquals("Expected equals brief statistics",
                new long[]{43, 274, 28, 3, 3}, statistics.briefStatistics());

        final BiConsumer<Object[], AbstractStatistic<?>> test = (expected, stats) -> {
            Assert.assertEquals("Expected equals number of occurrences", expected[0], stats.getOccurrences());
            Assert.assertEquals("Expected equals number of different occurrences", expected[1], stats.getDifferent());
            Assert.assertEquals("Expected equals min value", expected[2], stats.getMinValue());
            Assert.assertEquals("Expected equals max value", expected[3], stats.getMaxValue());
            boolean hasLength = false;
            try {
                Assert.assertEquals("Expected equals value with min length", expected[4], stats.getMinLength());
                Assert.assertEquals("Expected equals value with max length", expected[5], stats.getMaxLength());
                hasLength = true;
            } catch (final UnsupportedOperationException ignored) {}
            Assert.assertEquals("Expected equals average", expected[hasLength ? 6 : 4], stats.getAverage());
        };

        test.accept(new Object[]{43L, 43L,
                "Аргументы командной строки: локаль текста, локаль вывода, файл с текстом, файл отчета.",
                "Число чисел: 40.",
                "Число дат: 3.",
                "Для каждой категории должна собираться следующая статистика: число вхождений, число различных значений, минимальное значение, максимальное значение, минимальная длина, максимальная длина, среднее значение/длина.",
                "55,465"
        }, statistics.sentenceStatistic());

        test.accept(new Object[]{274L, 155L,
                "GK",
                "языках",
                "с",
                "TextStatistics",
                "6,712"
        }, statistics.wordStatistic());

        test.accept(new Object[]{28L, 17L,
                -12345.0,
                12345.67,
                "54,09"
        }, statistics.numberStatistic());

        test.accept(new Object[]{3L, 3L,
                100.0,
                345.67,
                "$222.83"
        }, statistics.moneyStatistic());
    }

    @Test
    public void stressTestNumbers() {
        final Random r = new Random();
        final Supplier<String> separator = () -> switch (r.nextInt(3)) {
            case 0 -> "\t";
            case 1 -> " ";
            case 2 -> "\n";
            default -> "?";
        };

        final String inputFilePath = TEST_FOLDER + "numbers.txt";
        try (final var writer = new BufferedWriter(Files.newBufferedWriter(Paths.get(inputFilePath), StandardCharsets.UTF_8))) {
            IntStream.iterate(0, i -> i < 10_000, i -> i + 1)
                    .forEach(i -> {
                        try {
                            writer.write(i + "" + separator.get());
                        } catch (final IOException ignored) {
                            // do nothing
                        }
                    });
        } catch (final IOException e) {
            Assert.fail("Rerun this test, please. Test generation went wrong");
        }

        final TextStatistics textStatistics = TextStatistics.statisticsOf(RUSSIAN_LOCALE, RUSSIAN_LOCALE,
                inputFilePath, TEST_FOLDER + "numbers_result.txt");

        textStatistics.updateStatistics();

        textStatistics.writeStatistics();
    }
}
