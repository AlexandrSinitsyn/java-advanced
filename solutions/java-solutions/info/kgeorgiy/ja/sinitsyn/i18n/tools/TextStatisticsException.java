package info.kgeorgiy.ja.sinitsyn.i18n.tools;

/**
 * Text statistic exception
 *
 * @author AlexSin
 */
public class TextStatisticsException extends RuntimeException {

    /** Primary constructor (containing only message of the exception) */
    public TextStatisticsException(final String message) {
        super(message);
    }

    /** Secondary constructor (containing both message and cause of the exception) */
    public TextStatisticsException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
