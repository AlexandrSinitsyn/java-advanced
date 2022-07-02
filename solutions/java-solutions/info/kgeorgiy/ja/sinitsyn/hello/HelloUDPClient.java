package info.kgeorgiy.ja.sinitsyn.hello;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Basic UDP client
 *
 * @author AlexSin
 */
public class HelloUDPClient extends AbstractHelloUDPClient {

    /**
     * Main method to run {@link HelloUDPClient} as a self-sufficient program
     *
     * @param args argument from console. You should pass five arguments. All
     *             of them are described in {@link #run(String, int, String, int, int)}
     *
     * @see #run(String, int, String, int, int)
     */
    public static void main(final String... args) {
        runMain(new HelloUDPClient(), args);
    }

    @Override
    protected void runImpl(final InetSocketAddress address, final MessageProcessor messageProcessor) {
        final var service = Executors.newFixedThreadPool(messageProcessor.threads());

        IntStream.range(0, messageProcessor.threads()).<Runnable>mapToObj(index -> () -> {
            try (final var socket = new DatagramSocket()) {
                socket.setSoTimeout(100);

                final int bufferSize = socket.getReceiveBufferSize();
                final var request = new DatagramPacket(new byte[]{}, 0, address);
                final var response = new DatagramPacket(new byte[bufferSize], bufferSize, address);

                for (int i = 0; i < messageProcessor.requests(); i++) {
                    final var message = messageProcessor.generate(index, i);
                    request.setData(message.getBytes(StandardCharsets.UTF_8));

                    while (true) {
                        try {
                            socket.send(request);
                            socket.receive(response);
                        } catch (final IOException e) {
                            log("Failed to send packet: " + i + " in thread " + index, e, true);
                        }

                        final var result = packetToString(response);

                        if (messageProcessor.isCorrect(result, index, i)) {
                            log(result, null, true);
                            break;
                        }
                    }
                }
            } catch (final SocketException e) {
                log("Socket is broken", e, false);
            }
        }).forEach(service::submit);

        service.shutdownNow();

        try {
            //noinspection ResultOfMethodCallIgnored
            service.awaitTermination((long) messageProcessor.threads() * messageProcessor.requests(), TimeUnit.MINUTES);
        } catch (final InterruptedException e) {
            log("Interrupted while waiting", e, false);
        }
    }
}
