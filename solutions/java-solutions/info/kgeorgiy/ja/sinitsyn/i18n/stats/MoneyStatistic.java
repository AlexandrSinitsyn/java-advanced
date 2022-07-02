package info.kgeorgiy.ja.sinitsyn.i18n.stats;

import java.text.*;
import java.util.Locale;
import java.util.Map;

/**
 * Date statistic
 *
 * @author AlexSin
 *
 * @see AbstractStatistic
 */
public final class MoneyStatistic extends AbstractStatistic<Double> {

    /**
     * Primary constructor to create statistic with default locale
     *
     * @see Locale
     * @see Locale#getDefault()
     */
    public MoneyStatistic() {
        super(false);
    }

    /**
     * Secondary constructor to create statistic with specific input
     * and output locales
     *
     * @see Locale
     */
    public MoneyStatistic(final Locale inputLocale, final Locale outputLocale) {
        super(false, inputLocale, outputLocale);
    }

    @Override
    protected BreakIterator spliterator() {
        return BreakIterator.getWordInstance(inputLocale);
    }

    @Override
    protected Double parse(final String content, final String word, final ParsePosition position) throws ParseException {
        final Number parsed = NumberFormat.getCurrencyInstance(new Locale("ru", "RU")).parse(content, position);

        if (parsed == null) {
            throw new ParseException("Expected currency", position.getIndex());
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

        return NumberFormat.getCurrencyInstance(outputLocale).format(d);
    }

    @Override
    public String getAverage() {
        return formatObject(statistics.getAverage());
    }
}
