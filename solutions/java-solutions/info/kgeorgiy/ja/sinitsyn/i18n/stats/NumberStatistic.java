package info.kgeorgiy.ja.sinitsyn.i18n.stats;

import java.text.*;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Date statistic
 *
 * @author AlexSin
 *
 * @see AbstractStatistic
 */
public final class NumberStatistic extends AbstractStatistic<Double> {

    private final DateStatistic dateStatistic = new DateStatistic(inputLocale, outputLocale);
    private final MoneyStatistic moneyStatistic = new MoneyStatistic(inputLocale, outputLocale);

    /**
     * Primary constructor to create statistic with default locale
     *
     * @see Locale
     * @see Locale#getDefault()
     */
    public NumberStatistic() {
        super(false);
    }

    /**
     * Secondary constructor to create statistic with specific input
     * and output locales
     *
     * @see Locale
     */
    public NumberStatistic(final Locale inputLocale, final Locale outputLocale) {
        super(false, inputLocale, outputLocale);
    }

    @Override
    protected BreakIterator spliterator() {
        return BreakIterator.getWordInstance(inputLocale);
    }

    @Override
    protected Double parse(final String content, final String word, final ParsePosition position) throws ParseException {
        final AtomicBoolean isNotNumber = new AtomicBoolean(false);

        final Consumer<AbstractStatistic<?>> testNotNumber = statistic -> {
            try {
                statistic.parse(content, word, position);
                isNotNumber.set(true);
            } catch (final ParseException ignored) {}
        };

        testNotNumber.accept(dateStatistic);
        testNotNumber.accept(moneyStatistic);

        if (isNotNumber.get()) {
            throw new ParseException("Date is not a number", 0);
        }

        final Number parsed = NumberFormat.getNumberInstance(inputLocale).parse(content, position);

        if (parsed == null) {
            throw new ParseException("Expected number", position.getIndex());
        }

        return parsed.doubleValue();
    }

    @Override
    protected Map.Entry<Boolean, Boolean> isLess(final Double current, final Double min) {
        return Map.entry(current < min, false);
    }

    @Override
    protected Map.Entry<Boolean, Boolean> isGreater(final Double current, final Double max) {
        return Map.entry(current > max, false);
    }

    @Override
    protected void everyWord(final Double d) {
        statistics.accept(d);
    }

    @Override
    public String formatObject(final Double d) {
        if (d == null) {
            return null;
        }

        return formatNumber(d);
    }
}
