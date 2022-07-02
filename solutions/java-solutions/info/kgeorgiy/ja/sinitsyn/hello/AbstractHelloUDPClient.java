package info.kgeorgiy.ja.sinitsyn.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 * Abstract UDP client
 *
 * @author AlexSin
 */
public abstract class AbstractHelloUDPClient implements HelloClient, HelloUDPParticipant {

    /** Call of this method should be the only content of {@code main(String[] args)} */
    protected static void runMain(final AbstractHelloUDPClient instance, final String... args) {
        instance.parseArgs(args, new Class<?>[]{String.class, int.class, String.class, int.class, int.class}, "run");
    }

    /** Generates message by thread number and request number */
    protected String generateRequest(final String prefix, final int thread, final int request) {
        return prefix + thread + "_" + request;
    }

    /** Interface to generate message and check that response from server is correct */
    protected interface MessageProcessor {
        /** method to generate request to server */
        String generate(final int thread, final int request);

        /** method to generate request to server */
        default String generate(final Map.Entry<Integer, Integer> state) {
            return generate(state.getKey(), state.getValue());
        }

        /** method to check response correctness */
        boolean isCorrect(final String response, final int thread, final int request);

        /** threads number */
        int threads();

        /** requests number */
        int requests();
    }

    /**
     * Method that will be called right after some preparations in
     * {@link #run(String, int, String, int, int)} in {@link AbstractHelloUDPClient}
     *
     * @param address address where client should send messages
     * @param generateMessage {@link MessageProcessor}
     *
     * @see InetSocketAddress
     * @see MessageProcessor
     * @see #run(String, int, String, int, int)
     */
    protected abstract void runImpl(final InetSocketAddress address, final MessageProcessor generateMessage);

    /**
     * {@inheritDoc}
     *
     * @param host the name or ip-address of the computer where the server is working
     * @param port the port-number where requests should be sent
     * @param prefix the prefix of each request (string)
     * @param threads the number of parallel threads to send requests
     * @param requests the number of requests in each thread
     */
    @Override
    public void run(final String host, final int port, final String prefix, final int threads, final int requests) {
        final var address = new InetSocketAddress(host, port);

        runImpl(address, new MessageProcessor() {
            @Override
            public String generate(final int thread, final int request) {
                return generateRequest(prefix, thread, request);
            }

            @Override
            public boolean isCorrect(final String response, final int thread, final int request) {
                return response.matches("\\D*%d\\D*%d\\D*".formatted(thread, request));
            }

            @Override
            public int threads() {
                return threads;
            }

            @Override
            public int requests() {
                return requests;
            }
        });
    }
}
