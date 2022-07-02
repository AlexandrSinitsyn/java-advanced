package info.kgeorgiy.ja.sinitsyn.hello;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.function.Function;

/**
 * Basic UDP Server
 *
 * @author AlexSin
 */
public class HelloUDPServer extends AbstractHelloUDPServer {

    private DatagramSocket socket;

    /**
     * Main method to run {@link HelloUDPServer} as a self-sufficient program
     *
     * @param args argument from console. You should pass two arguments. All
     *             of them are described in {@link #start(int, int)}
     *
     * @see #start(int, int)
     */
    public static void main(final String[] args) {
        AbstractHelloUDPServer.runMain(new HelloUDPServer(), args);
    }

    private DatagramPacket emptyDatagramPacket(final int size) throws SocketException {
        return new DatagramPacket(new byte[size], size);
    }

    @Override
    protected void startImpl(final InetSocketAddress address) {
        try {
            socket = new DatagramSocket(address);

            final Function<DatagramPacket, Runnable> processDatagram = dp -> () -> {
                final var response = generateResponse(dp);

                try {
                    socket.send(response);
                } catch (final IOException e) {
                    log("Failed IO while sending '" + response + "'", e, true);
                }
            };

            receivers.submit(() -> {
                while (!socket.isClosed() && !Thread.interrupted()) {
                    try {
                        final DatagramPacket dp = emptyDatagramPacket(socket.getReceiveBufferSize());

                        socket.receive(dp);

                        service.submit(processDatagram.apply(dp));
                    } catch (final IOException e) {
                        log("Failed to receive datagram", e, true);
                    }
                }
            });
        } catch (final SocketException e) {
            log("Socket is closed", e, false);
        }
    }

    @Override
    protected void closeImpl(final UDPException caught) {
        socket.close();
    }
}
