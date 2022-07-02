package info.kgeorgiy.ja.sinitsyn.walk;

public class WalkException extends RuntimeException {

    public WalkException(String message) {
        super(message);
    }

    public WalkException(String message, Throwable cause) {
        super(message, cause);
    }
}
