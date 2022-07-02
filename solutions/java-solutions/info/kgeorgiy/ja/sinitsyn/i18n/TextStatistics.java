package info.kgeorgiy.ja.sinitsyn.i18n;

import info.kgeorgiy.ja.sinitsyn.i18n.stats.*;
import info.kgeorgiy.ja.sinitsyn.i18n.tools.TextStatisticsException;
import info.kgeorgiy.ja.sinitsyn.i18n.tools.Tools;
import org.junit.Assert;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * Utility class for text statistics. To create an instance use
 * {@link #statisticsOf(Locale, Locale, Path, Path)} or
 * {@link #statisticsOf(Locale, Locale, String, String)}. Input
 * and output is mentioned to be localized, so {@code $100} will
 * be parsed as currency for {@link Locale#US} while {@code "123 €"}
 * will not.
 *
 * @see Locale
 *
 * @author AlexSin
 */
public final class TextStatistics {

    private final SentenceStatistic sentenceStatistic;
    private final WordStatistic wordStatistic;
    private final NumberStatistic numberStatistic;
    private final MoneyStatistic moneyStatistic;
    private final DateStatistic dateStatistic;

    private final AbstractStatistic<?>[] allStatistics;

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final Locale inputLocale;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final Locale outputLocale;
    private final Path inputFile;
    private final Path outputFile;

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final ResourceBundle bundle;

    /** Utility class */
    private TextStatistics(final Locale inputLocale, final Locale outputLocale, final Path inputFile, final Path outputFile) {
        this.inputLocale = inputLocale;
        this.outputLocale = outputLocale;
        this.inputFile = inputFile;
        this.outputFile = outputFile;

        bundle = Tools.getBundle("textStatistics", outputLocale);

        sentenceStatistic = new SentenceStatistic(inputLocale, outputLocale);
        wordStatistic = new WordStatistic(inputLocale, outputLocale);
        numberStatistic = new NumberStatistic(inputLocale, outputLocale);
        moneyStatistic = new MoneyStatistic(inputLocale, outputLocale);
        dateStatistic = new DateStatistic(inputLocale, outputLocale);

        allStatistics = new AbstractStatistic<?>[]{
                sentenceStatistic,
                wordStatistic,
                numberStatistic,
                moneyStatistic,
                dateStatistic,
        };
    }

    private static <R> R parseArgument(final String[] args, final int index, final Function<String, R> f) {
        try {
            return f.apply(args[index]);
        } catch (final Exception e) {
            throw new TextStatisticsException("Illegal argument passed in position " + index +
                    " of args: " + Arrays.toString(args), e);
        }
    }

    /**
     * Main method to run {@link TextStatistics} as a self-sufficient program
     *
     * @param args command line arguments. You should pass exactly four arguments as this:
     *             <ul>
     *             <li>input locale. Ex: ru-RU, en-US</li>
     *             <li>output locale. Ex: ru-RU, en-US</li>
     *             <li>input file. Ex: ../examples/input.txt</li>
     *             <li>output file. Ex: ../examples/output.txt</li>
     *             </ul>
     *
     * @see Locale
     * @see TextStatistics
     * @see #updateStatistics()
     */
    public static void main(final String[] args) {
        final ResourceBundle bundle = Tools.getBundle("textStatistics", Locale.forLanguageTag("ru-RU"));

        Assert.assertNotNull(bundle.getString("non-null-array"), args);
        Assert.assertTrue(bundle.getString("non-null-args"), Arrays.stream(args).noneMatch(Objects::isNull));
        Assert.assertEquals(bundle.getString("args-length"), 4, args.length);

        final Locale inputLocale = parseArgument(args, 0, Locale::forLanguageTag);
        final Locale outputLocale = parseArgument(args, 1, Locale::forLanguageTag);
        final Path fileIn = parseArgument(args, 2, Paths::get);
        final Path fileOut = parseArgument(args, 3, Paths::get);

        final TextStatistics statistics = TextStatistics.statisticsOf(inputLocale, outputLocale, fileIn, fileOut);

        statistics.updateStatistics();

        statistics.writeStatistics();
    }

    /**
     * Writing the response of all statistics (from the given
     * text) to output file ({@link #outputFile})
     *
     * @throws IOException if writing went wrong
     */
    public void writeStatisticsOrThrow() throws IOException {
        try (final BufferedWriter writer = new BufferedWriter(Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8))) {
            final long[] briefStatistics = briefStatistics();

            final ResourceBundle bundle = Tools.getBundle("briefStatistics", outputLocale);

            final String pattern = "\t%s: %d.\n";
            final String brief = String.format("%s \"%s\"\n%s\n" + pattern.repeat(5),
                    bundle.getString("analyzed-file"), inputFile.getFileName(),
                    bundle.getString("all-statistics"),
                    bundle.getString("sentences"), briefStatistics[0],
                    bundle.getString("words"), briefStatistics[1],
                    bundle.getString("numbers"), briefStatistics[2],
                    bundle.getString("money"), briefStatistics[3],
                    bundle.getString("dates"), briefStatistics[4]);

            writer.write(brief);
            for (final String statistic : Arrays.stream(allStatistics).map(AbstractStatistic::toString).toList()) {
                writer.write(statistic);
            }
        }
    }

    /**
     * Same as {@link #writeStatisticsOrThrow()} but with handling exceptions
     * if they occur
     */
    public void writeStatistics() {
        try {
            writeStatisticsOrThrow();
        } catch (final IOException e) {
            Tools.log("I/O exception occurred while writing statistics to file \"" + outputFile + "\"", e, true);
        }
    }

    /** Read {@link #inputFile} and parse statistics */
    public void updateStatistics() {
        try (final BufferedReader reader = new BufferedReader(Files.newBufferedReader(inputFile))) {
            final List<String> content = new ArrayList<>();

            String line;
            while ((line = reader.readLine()) != null) {
                content.add(line);
            }

            final long start = System.currentTimeMillis();
            System.out.println("Started parsing...");
            parseContent(content);
            System.out.println("Completed in: " + (System.currentTimeMillis() - start) + "ms");
        } catch (final IOException e) {
            Tools.log("I/O Exception occurred while reading from file \"" + inputFile + "\"", e, true);
        }
    }

    private void parseContent(final List<String> content) {
        final int threads = allStatistics.length;

        final ExecutorService service = Executors.newFixedThreadPool(threads);

        final String wholeText = String.join("\n", content);

        final CountDownLatch cd = new CountDownLatch(threads);

        Arrays.stream(allStatistics).forEach(statistic -> service.submit(() -> {
            System.out.printf("\t> %s started...\n", statistic.getClass().getSimpleName());
            statistic.reset();
            statistic.process(wholeText);
            System.out.printf("< %s done\n", statistic.getClass().getSimpleName());
            cd.countDown();
        }));

        try {
            cd.await();
            service.shutdownNow();
            //noinspection ResultOfMethodCallIgnored
            service.awaitTermination(threads * 100L, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            Tools.log("Reading threads were not closed correctly", e, true);
        }
    }

    /** Brief statistics for the given text. Just number of
     * occurrences for all statistics */
    public long[] briefStatistics() {
        return Arrays.stream(allStatistics).mapToLong(AbstractStatistic::getOccurrences).toArray();
    }

    /** {@link SentenceStatistic} for {@link #inputFile}. Only after {@link #updateStatistics()}*/
    public SentenceStatistic sentenceStatistic() {
        return sentenceStatistic;
    }

    /** {@link WordStatistic} for {@link #inputFile}. Only after {@link #updateStatistics()}*/
    public WordStatistic wordStatistic() {
        return wordStatistic;
    }

    /** {@link NumberStatistic} for {@link #inputFile}. Only after {@link #updateStatistics()}*/
    public NumberStatistic numberStatistic() {
        return numberStatistic;
    }

    /** {@link MoneyStatistic} for {@link #inputFile}. Only after {@link #updateStatistics()}*/
    public MoneyStatistic moneyStatistic() {
        return moneyStatistic;
    }

    /** {@link DateStatistic} for {@link #inputFile}. Only after {@link #updateStatistics()}*/
    public DateStatistic dateStatistic() {
        return dateStatistic;
    }

    /**
     * Create an instance of {@link TextStatistics} by locale and paths to files
     *
     * @param inputLocale locale of input text
     * @param outputLocale locale of statistic's response
     * @param inputFilePath string path to input file
     * @param outputFilePath string path to output file
     * @return instance of {@link TextStatistics}
     *
     * @see Locale
     * @see TextStatistics
     */
    public static TextStatistics statisticsOf(final Locale inputLocale, final Locale outputLocale,
                                              final String inputFilePath, final String outputFilePath) {
        final Path inputFile = parseArgument(new String[]{inputFilePath}, 0, Paths::get);
        final Path outputFile = parseArgument(new String[]{outputFilePath}, 0, Paths::get);

        return statisticsOf(inputLocale, outputLocale, inputFile, outputFile);
    }

    /**
     * Create an instance of {@link TextStatistics} by locale and files
     *
     * @param inputLocale locale of input text
     * @param outputLocale locale of statistic's response
     * @param inputFile input file
     * @param outputFile output file
     * @return instance of {@link TextStatistics}
     *
     * @see Locale
     * @see Path
     * @see TextStatistics
     */
    public static TextStatistics statisticsOf(final Locale inputLocale, final Locale outputLocale,
                                              final Path inputFile, final Path outputFile) {
        return new TextStatistics(inputLocale, outputLocale, inputFile, outputFile);
    }
}
