package info.kgeorgiy.ja.sinitsyn.hello;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * Nonblocking UDP client
 *
 * @author AlexSin
 */
public class HelloUDPNonblockingClient extends AbstractHelloUDPClient {

    /**
     * Main method to run {@link HelloUDPNonblockingClient} as a self-sufficient program
     *
     * @param args argument from console. You should pass five arguments. All
     *             of them are described in {@link #run(String, int, String, int, int)}
     *
     * @see #run(String, int, String, int, int)
     */
    public static void main(final String... args) {
        runMain(new HelloUDPNonblockingClient(), args);
    }

    @Override
    protected void runImpl(final InetSocketAddress address, final MessageProcessor messageProcessor) {
        try {
            final Selector selector = Selector.open();

            final List<DatagramChannel> channels = new ArrayList<>();
            IntStream.range(0, messageProcessor.threads()).mapToObj(i -> {
                final DatagramChannel channel;

                try {
                    channel = DatagramChannel.open();
                    channel.configureBlocking(false);
                    channel.register(selector, SelectionKey.OP_WRITE, Map.entry(i, 0));
                } catch (final IOException e) {
                    throw new UncheckedIOException(e);
                }

                return channel;
            }).forEach(channels::add);

            while (!Thread.interrupted() && channels.stream().anyMatch(DatagramChannel::isOpen)) {
                selector.select(100);

                final Set<SelectionKey> arrived = selector.selectedKeys();
                if (arrived.isEmpty()) {
                    selector.keys().forEach(key -> key.interestOps(SelectionKey.OP_WRITE));
                }

                arrived.forEach(key -> {
                    try {
                        @SuppressWarnings("unchecked")
                        final var state = (Map.Entry<Integer, Integer>) key.attachment();
                        final DatagramChannel channel = channels.get(state.getKey());

                        if (state.getValue() >= messageProcessor.requests()) {
                            channel.close();
                            return;
                        }

                        if (key.isReadable()) {
                            final var response = ByteBuffer.allocate(channel.socket().getReceiveBufferSize());
                            channel.receive(response);

                            final String result = decodeResponse(response);

                            if (messageProcessor.isCorrect(result, state.getKey(), state.getValue())) {
                                log(result, null, true);
                                key.attach(Map.entry(state.getKey(), state.getValue() + 1));
                            }
                            key.interestOps(SelectionKey.OP_WRITE);
                        } else if (key.isWritable()) {
                            final String message = messageProcessor.generate(state);

                            final var request = ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8));

                            channel.send(request, address);
                            key.interestOps(SelectionKey.OP_READ);
                        }
                    } catch (final IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });

                arrived.clear();
            }

            for (final DatagramChannel channel : channels) {
                channel.close();
            }
        } catch (final IOException e) {
            log("", e, false);
        }
    }
}
