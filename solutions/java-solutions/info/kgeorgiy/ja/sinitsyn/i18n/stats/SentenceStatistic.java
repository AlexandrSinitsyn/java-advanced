package info.kgeorgiy.ja.sinitsyn.i18n.stats;

import java.text.BreakIterator;
import java.text.Collator;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Date statistic
 *
 * @author AlexSin
 *
 * @see AbstractStatistic
 */
public final class SentenceStatistic extends AbstractStatistic<String> {

    private final Collator collator;

    /**
     * Primary constructor to create statistic with default locale
     *
     * @see Locale
     * @see Locale#getDefault()
     */
    public SentenceStatistic() {
        super(true);
        collator = Collator.getInstance(inputLocale);
    }

    /**
     * Secondary constructor to create statistic with specific input
     * and output locales
     *
     * @see Locale
     */
    public SentenceStatistic(final Locale inputLocale, final Locale outputLocale) {
        super(true, inputLocale, outputLocale);
        collator = Collator.getInstance(inputLocale);
    }

    @Override
    protected BreakIterator spliterator() {
        return BreakIterator.getSentenceInstance(inputLocale);
    }

    @Override
    protected String parse(final String content, final String word, final ParsePosition position) {
        return word;
    }

    @Override
    protected Entry<Boolean, Boolean> isLess(final String current, final String min) {
        return Map.entry(collator.compare(current, min) < 0, current.length() < min.length());
    }

    @Override
    protected Entry<Boolean, Boolean> isGreater(final String current, final String max) {
        return Map.entry(collator.compare(current, max) > 0, current.length() > max.length());
    }

    @Override
    protected void everyWord(final String s) {
        statistics.accept(s.length());
    }

    @Override
    public String formatObject(final String s) {
        return "\"" + s + "\"";
    }
}
