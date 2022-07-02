package info.kgeorgiy.ja.sinitsyn.i18n.stats;

import java.text.*;
import java.util.*;

/**
 * Date statistic
 *
 * @author AlexSin
 *
 * @see AbstractStatistic
 */
public final class DateStatistic extends AbstractStatistic<Date> {

    /**
     * Primary constructor to create statistic with default locale
     *
     * @see Locale
     * @see Locale#getDefault()
     */
    public DateStatistic() {
        super(false);
    }

    /**
     * Secondary constructor to create statistic with specific input
     * and output locales
     *
     * @see Locale
     */
    public DateStatistic(final Locale inputLocale, final Locale outputLocale) {
        super(false, inputLocale, outputLocale);
    }

    @Override
    protected BreakIterator spliterator() {
        return BreakIterator.getWordInstance(inputLocale);
    }

    @Override
    protected Date parse(final String content, final String word, final ParsePosition position) throws ParseException {
        for (final int format : new int[]{DateFormat.LONG, DateFormat.MEDIUM, DateFormat.SHORT}) {
            final Date parsed = DateFormat.getDateInstance(format, inputLocale).parse(content, position);

            if (parsed != null) {
                return parsed;
            }
        }

        throw new ParseException("Expected date", position.getIndex());
    }

    @Override
    protected Map.Entry<Boolean, Boolean> isLess(final Date current, final Date min) {
        return Map.entry(current.before(min), false);
    }

    @Override
    protected Map.Entry<Boolean, Boolean> isGreater(final Date current, final Date max) {
        return Map.entry(current.after(max), false);
    }

    @Override
    protected void everyWord(final Date date) {
        statistics.accept(date.getTime());
    }

    @Override
    public String formatObject(final Date date) {
        if (date == null) {
            return null;
        }

        return DateFormat.getDateInstance(DateFormat.MEDIUM, outputLocale).format(date);
    }

    @Override
    public String getAverage() {
        return formatObject(new Date((long) statistics.getAverage()));
    }
}
