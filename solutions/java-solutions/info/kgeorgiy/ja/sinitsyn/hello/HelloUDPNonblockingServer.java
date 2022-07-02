package info.kgeorgiy.ja.sinitsyn.hello;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

/**
 * Nonblocking UDP server
 *
 * @author AlexSin
 */
public class HelloUDPNonblockingServer extends AbstractHelloUDPServer {

    private Selector selector;
    private DatagramChannel channel;

    /**
     * Main method to run {@link HelloUDPNonblockingServer} as a self-sufficient program
     *
     * @param args argument from console. You should pass two arguments. All
     *             of them are described in {@link #start(int, int)}
     *
     * @see #start(int, int)
     */
    public static void main(final String[] args) {
        AbstractHelloUDPServer.runMain(new HelloUDPNonblockingServer(), args);
    }

    @Override
    protected void startImpl(final InetSocketAddress address) {
        try {
            selector = Selector.open();
            channel = DatagramChannel.open();

            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
            channel.bind(address);
        } catch (final IOException e) {
            log("Selector or datagram channel can not be open", e, false);
        }

        receivers.submit(() -> {
            while (channel.isOpen() && !Thread.interrupted()) {
                try {
                    selector.select(key -> {
                        try {
                            final ByteBuffer buffer = ByteBuffer.allocate(channel.socket().getReceiveBufferSize());
                            final SocketAddress request = channel.receive(buffer);

                            service.submit(() -> {
                                final ByteBuffer response = generateResponse(buffer);

                                key.interestOps(SelectionKey.OP_WRITE);

                                try {
                                    channel.send(response, request);
                                } catch (final IOException e) {
                                    log("", e, false);
                                }
                            });
                        } catch (final IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
                } catch (final IOException e) {
                    log("I/O exception occurred", e, true);
                }
            }
        });
    }

    @Override
    protected void closeImpl(final UDPException caught) {
        try {
            selector.close();
            channel.close();
        } catch (final IOException e) {
            caught.addSuppressed(e);
        }
    }
}
