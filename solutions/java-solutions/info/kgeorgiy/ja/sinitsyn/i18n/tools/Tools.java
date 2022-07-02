package info.kgeorgiy.ja.sinitsyn.i18n.tools;

import info.kgeorgiy.ja.sinitsyn.i18n.TextStatistics;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Basic class with some useful tools
 *
 * @author AlexSin
 */
public final class Tools {

    /** Utility class */
    private Tools() {}

    /** Logging function */
    @SuppressWarnings("unused")
    public static void log(final String message, final Throwable exception, final boolean logging) {
        if (logging) {
            System.out.println(message + (exception == null ? "" : ": " + exception.getMessage()));
        } else {
            throw new TextStatisticsException(message, exception);
        }
    }

    /** Mark "method_name : line" where it was called */
    @SuppressWarnings("unused")
    public static void here() {
        here("");
    }

    /** Mark "method_name : line <<< message" where it was called */
    @SuppressWarnings("unused")
    public static void here(final String message) {
        final class FakeException extends RuntimeException {}

        try {
            throw new FakeException();
        } catch (final FakeException e) {
            final StackTraceElement method = e.getStackTrace()[1];

            System.out.printf("%s : %d <<< %s\n", method.getMethodName(), method.getLineNumber(), message);
        }
    }

    /**
     * Retrieves bundle from a {@code property} file
     *
     * @param fileName property file name
     * @param locale parameter to look for localized property file
     *               <p>Example:</p>
     *               <p><code>getBundle("statistic", Locale.US)</code></p>
     *               <p>this will look for file "statistic_en_US.properties" in resource folder</p>
     * @return  resource bundle from specific property file
     *
     * @see ResourceBundle
     */
    public static ResourceBundle getBundle(final String fileName, final Locale locale) {
        return ResourceBundle.getBundle(TextStatistics.class.getPackageName() + ".resources." + fileName, locale);
    }
}
