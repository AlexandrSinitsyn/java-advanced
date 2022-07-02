package info.kgeorgiy.ja.sinitsyn.hello;

import org.junit.Assert;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * Interface that each participant (server or client) should implement
 *
 * @author AlexSin
 */
public interface HelloUDPParticipant {

    /** UDP exception */
    final class UDPException extends RuntimeException {

        public UDPException(final String message) {
            super(message);
        }

        public UDPException(final String message, final Throwable throwable) {
            super(message, throwable);
        }
    }

    /** Logging function */
    default void log(final String message, final Throwable exception, final boolean logging) {
        if (logging) {
            System.out.println(message + (exception == null ? "" : ": " + exception.getMessage()));
        } else {
            throw new UDPException(message, exception);
        }
    }

    /**
     * Decode function
     *
     * @param response buffer to decode
     * @return decoded string
     *
     * @see ByteBuffer
     */
    default String decodeResponse(final ByteBuffer response) {
        response.flip();

        return StandardCharsets.UTF_8.decode(response).toString();
    }

    /** Mark "method_name : line" where it was called */
    @SuppressWarnings("unused")
    default void here() {
        here("");
    }

    /** Mark "method_name : line <<< message" where it was called */
    @SuppressWarnings("unused")
    default void here(final String message) {
        final class FakeException extends RuntimeException {}

        try {
            throw new FakeException();
        } catch (final FakeException e) {
            final StackTraceElement method = e.getStackTrace()[1];

            System.out.printf("%s : %d <<< %s\n", method.getMethodName(), method.getLineNumber(), message);
        }
    }

    /** Packet to string converter */
    default String packetToString(final DatagramPacket packet) {
        return new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
    }

    private int getInt(final String[] args, final int index) {
        try {
            return Integer.parseInt(args[index]);
        } catch (final NumberFormatException e) {
            throw new UDPException("Number should be passed", e);
        }
    }

    /** Method to process arguments from command line */
    default void parseArgs(final String[] args, final Class<?>[] types,
                                  final String methodName) {
        Assert.assertNotNull("Args can not be null", args);
        Assert.assertTrue("Args can not be null", Arrays.stream(args).noneMatch(Objects::isNull));
        Assert.assertEquals("HelloUDPClient expects " + types.length + " arguments to be passed", types.length, args.length);

        final var arguments = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            arguments[i] = types[i] == int.class ? getInt(args, i) : args[i];
        }

        try {
            final Class<?> clazz = Class.forName(this.getClass().getName());
            final var object = clazz.getDeclaredConstructor().newInstance();

            clazz.getDeclaredMethod(methodName, types).invoke(object, arguments);
        } catch (final ReflectiveOperationException e) {
            throw new UDPException("Exception in reflection", e);
        }
    }
}
