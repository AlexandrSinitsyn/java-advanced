package info.kgeorgiy.ja.sinitsyn.bank;

import info.kgeorgiy.ja.sinitsyn.bank.bank.Bank;
import info.kgeorgiy.ja.sinitsyn.bank.bank.RemoteBank;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Server
 *
 * @author AlexSin
 */
public final class Server {
    private final static int DEFAULT_PORT = 8888;

    /**
     * Method to start {@link Server} as a self-sufficient program
     *
     * @param args usage:
     *             <ul>
     *             <li>port - to server on this port</li>
     *             </ul>
     */
    public static void main(final String... args) {
        final int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;

        try {
            final Registry registry = LocateRegistry.createRegistry(port);

            final Bank bank = new RemoteBank(port);
            UnicastRemoteObject.exportObject(bank, port);
            registry.rebind("//localhost/bank", bank);
            System.out.println("Server started");
        } catch (final RemoteException e) {
            System.out.println("Cannot export object: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
