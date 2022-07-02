package info.kgeorgiy.ja.sinitsyn.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Abstract UDP server
 *
 * @author AlexSin
 */
public abstract class AbstractHelloUDPServer implements HelloServer, HelloUDPParticipant {

    /** Threads for processing received packets */
    protected ExecutorService service;

    /** Threads for receiving packets */
    protected ExecutorService receivers;

    /** Call of this method should be the only content of {@code main(String[] args)} */
    protected static void runMain(final AbstractHelloUDPServer instance, final String[] args) {
        instance.parseArgs(args, new Class<?>[]{int.class, int.class}, "start");
    }

    /** Generates response for given datagram
     * @see DatagramPacket */
    protected DatagramPacket generateResponse(final DatagramPacket dp) {
        final String message = "Hello, " + packetToString(dp);

        return new DatagramPacket(message.getBytes(StandardCharsets.UTF_8), message.length(), dp.getSocketAddress());
    }

    /** Generates response for given byteBuffer
     * @see ByteBuffer */
    protected ByteBuffer generateResponse(final ByteBuffer buffer) {
        final String message = "Hello, " + decodeResponse(buffer);

        buffer.clear();
        buffer.put(message.getBytes(StandardCharsets.UTF_8));
        buffer.limit(message.length());
        buffer.rewind();

        return buffer;
    }

    /**
     * Method that will be called after some preparations in method {@link #start(int, int)}
     *
     * @param address an address which socket should be connected on
     *
     * @see #start(int, int)
     * @see InetSocketAddress
     */
    protected abstract void startImpl(final InetSocketAddress address);

    /**
     * Method to close personal sockets, threads, etc.
     *
     * @param caught any exceptions that occurred during the method should be added
     *               to this as {@code suppressed} via {@link Exception#addSuppressed(Throwable)}.
     *               After all closing in the main method {@link #close()} if some troubles has
     *               happened, this exception will be shown to user.
     *
     * @see Exception#addSuppressed(Throwable)
     * @see #close()
     */
    protected abstract void closeImpl(final UDPException caught);

    /**
     * {@inheritDoc}
     *
     * @param port the port-number where requests would be taken
     * @param threads the number of parallel threads to process requests
     */
    @Override
    public void start(final int port, final int threads) {
        final var address = new InetSocketAddress(port);

        receivers = Executors.newSingleThreadExecutor();
        service = Executors.newFixedThreadPool(threads);

        startImpl(address);
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void close() {
        final var caught = new UDPException("Socket or helping thread is unclosed");

        closeImpl(caught);

        receivers.shutdownNow();
        service.shutdownNow();

        try {
            receivers.awaitTermination(1, TimeUnit.MINUTES);
            service.awaitTermination(1, TimeUnit.MINUTES);
        } catch (final InterruptedException e) {
            caught.addSuppressed(e);
        }

        if (caught.getSuppressed().length != 0) {
            throw caught;
        }
    }
}
