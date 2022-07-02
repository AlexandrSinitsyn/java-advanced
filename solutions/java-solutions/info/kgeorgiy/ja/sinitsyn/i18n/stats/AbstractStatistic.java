package info.kgeorgiy.ja.sinitsyn.i18n.stats;

import info.kgeorgiy.ja.sinitsyn.i18n.tools.Tools;

import java.text.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/**
 * Basic class to provide statistic over a text for any comparable values.
 * It calculates:
 * <ul>
 *     <li>occurrences</li>
 *     <li>different objects count</li>
 *     <li>min value</li>
 *     <li>max value</li>
 *     <li>min length (for those values that have this parameter. For example,
 *     {@code string})</li>
 *     <li>min length</li>
 *     <li>average length/value</li>
 * </ul>
 *
 * @param <T> type of variable to search for
 *
 * @author AlexSin
 */
public abstract sealed class AbstractStatistic<T extends Comparable<T>>
        permits WordStatistic, SentenceStatistic, NumberStatistic, MoneyStatistic, DateStatistic {

    private AtomicLong occurrences;
    private AtomicLong different;
    private T minValue;
    private T maxValue;
    private T minLength;
    private T maxLength;

    /** Summary statistics for getting min and max and average value */
    protected DoubleSummaryStatistics statistics;

    private final boolean hasLength;
    /** Input locale */
    protected final Locale inputLocale;
    /** Output locale */
    protected final Locale outputLocale;

    /**
     * Resource bundle for output locale
     *
     * @see ResourceBundle
     */
    protected final ResourceBundle bundle;

    /**
     * Constructor to create abstract statistic
     *
     * @param hasLength indicator that shows is it possible to get length of this object
     */
    protected AbstractStatistic(final boolean hasLength) {
        this(hasLength, Locale.getDefault(), Locale.getDefault());
    }

    /**
     * Constructor to create abstract statistic
     *
     * @param hasLength indicator that shows is it possible to get length of this object
     * @param inputLocale input locale
     * @param outputLocale output locale
     */
    protected AbstractStatistic(final boolean hasLength, final Locale inputLocale, final Locale outputLocale) {
        this.hasLength = hasLength;
        this.inputLocale = inputLocale;
        this.outputLocale = outputLocale;

        reset();

        final String simpleName = this.getClass().getSimpleName();
        bundle = Tools.getBundle(
                Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1), outputLocale);
    }

    /**
     * Method to study the given text
     *
     * @param content whole text to study
     */
    public void process(final String content) {
        if (content.isBlank()) {
            return;
        }

        final BreakIterator iterator = spliterator();

        iterator.setText(content);

        final var position = new ParsePosition(0);
        final Stream<T> split = Stream.iterate(Map.entry(iterator.first(), iterator.next()),
                        e -> e.getValue() != BreakIterator.DONE,
                        e -> Map.entry(iterator.current(), iterator.next()))
                .<Optional<T>>map(e -> {
                    if (position.getIndex() > e.getKey()) {
                            return Optional.empty();
                    }
                    position.setIndex(e.getKey());
                    final String word = content.substring(e.getKey(), e.getValue());

                    try {
                        final List<String> excl = List.of("\n", "\t", "    ", "  ");
                        final T parsed = parse(content, excl.stream()
                                .reduce(word.trim(), (w, rep) -> w.replace(rep, " ")), position);

                        if (minValue == null) {
                            minValue = minLength = maxValue = maxLength = parsed;
                        }

                        minValue = isLess(parsed, minValue).getKey() ? parsed : minValue;
                        minLength = isLess(parsed, minLength).getValue() ? parsed : minLength;

                        maxValue = isGreater(parsed, maxValue).getKey() ? parsed : maxValue;
                        maxLength = isGreater(parsed, maxLength).getValue() ? parsed : maxLength;

                        everyWord(parsed);

                        return Optional.of(parsed);
                    } catch (final ParseException ignored) {
                        return Optional.empty();
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get);

        different.set(split.distinct().count());
        occurrences.set(statistics.getCount());
    }

    /**
     * Spliterator of the text, that goes over positions that bounds values of wished type
     *
     * @see BreakIterator
     */
    protected abstract BreakIterator spliterator();

    /**
     * Tries to parse given word if it fits {@link #inputLocale}.
     *
     * <p>For example:</p>
     * <p>{@code "$123"} - is a correct currency for {@link Locale#US}</p>
     * <p>but {@code "123 €"} - is not a correct currency, so this method
     * will throw an exception to show it</p>
     *
     * @param content whole text
     * @param word current word
     * @param position pointer to words start position
     * @return parsed word if only it can be parsed with given {@link #inputLocale}.
     * Otherwise, this method should throw an exception
     * @throws ParseException if this word is not wished value
     *
     * @see Locale
     * @see ParsePosition
     */
    protected abstract T parse(final String content, final String word, final ParsePosition position) throws ParseException;

    /** Checks if current value less that found minimum. Returns pair: &lt;is less, is smaller (length)&gt; */
    protected abstract Map.Entry<Boolean, Boolean> isLess(final T current, final T min);

    /** Checks if current value greater that found maximum. Returns pair: &lt;is greater, is larger (length)&gt; */
    protected abstract Map.Entry<Boolean, Boolean> isGreater(final T current, final T max);

    /** This method is applied to every value, found in text */
    protected abstract void everyWord(final T t);

    /** Total number of found values */
    public long getOccurrences() {
        return occurrences.get();
    }

    /** Number different value */
    public long getDifferent() {
        return different.get();
    }

    /** Min value */
    public T getMinValue() {
        return minValue;
    }

    /** Max value */
    public T getMaxValue() {
        return maxValue;
    }

    /**
     * The value that has min length (if applicable else it will throw {@link UnsupportedOperationException})
     *
     * @return found value with minimal length
     * @throws UnsupportedOperationException if this type of value does not have "length"-parameter
     */
    public T getMinLength() {
        if (hasLength) {
            return minLength;
        } else {
            throw new UnsupportedOperationException("No length can be provided for this statistics");
        }
    }

    /**
     * The value that has max length (if applicable else it will throw {@link UnsupportedOperationException})
     *
     * @return found value with maximal length
     * @throws UnsupportedOperationException if this type of value does not have "length"-parameter
     */
    public T getMaxLength() {
        if (hasLength) {
            return maxLength;
        } else {
            throw new UnsupportedOperationException("No length can be provided for this statistics");
        }
    }

    /**
     * Convert value to string with localized format
     *
     * @param t value to convert
     * @return localized string representation of given value
     */
    public abstract String formatObject(final T t);

    /** Formats number according to output locale */
    protected String formatNumber(final Number number) {
        return new DecimalFormat("###,##0.0##").format(number);
    }

    /** The average value */
    public String getAverage() {
         return formatNumber(statistics.getAverage());
    }

    /** Reset statistic to zero. Reset progress */
    public void reset() {
        occurrences = new AtomicLong(0);
        different = new AtomicLong(0);
        minValue = null;
        maxValue = null;
        minLength = null;
        maxLength = null;
        statistics = new DoubleSummaryStatistics();
    }

    @Override
    public String toString() {
        String format = "%s\n\t%s: %s (%s %s).\n".formatted(
                bundle.getString("statistics"),
                bundle.getString("count"), formatNumber(getOccurrences()),
                formatNumber(getDifferent()), bundle.getString("different"));
        format = (format + "\t%s: %s.\n".repeat(2)).formatted(
                bundle.getString("min-value"), formatObject(minValue),
                bundle.getString("max-value"), formatObject(maxValue));

        if (hasLength) {
            format = (format + "\t%s: %s (%s).\n".repeat(2))
                    .formatted(bundle.getString("min-length"), formatNumber(statistics.getMin()), formatObject(minLength),
                            bundle.getString("max-length"), formatNumber(statistics.getMax()), formatObject(maxLength));
        }

        format = (format + "\t%s: %s.\n").formatted(bundle.getString("average"), getAverage());

        return format;
    }
}
