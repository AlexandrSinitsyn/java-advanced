package info.kgeorgiy.ja.sinitsyn.bank;

import info.kgeorgiy.ja.sinitsyn.bank.account.Account;
import info.kgeorgiy.ja.sinitsyn.bank.bank.Bank;
import info.kgeorgiy.ja.sinitsyn.bank.person.Person;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.function.BiFunction;

/**
 * Client
 *
 * @author AlexSin
 */
public final class Client {
    private final static int DEFAULT_PORT = 8888;

    /** Utility class. */
    private Client() {}

    /**
     * Method to start {@link Client} as a self-sufficient program
     *
     * @param args usage:
     *             <ul>
     *             <li>port - port where server is hosted</li>
     *             <li>first name - first name of the client</li>
     *             <li>last name - last name of the client</li>
     *             <li>passport - passport of the client</li>
     *             <li>account id - the id of the account to get access to</li>
     *             </ul>
     */
    public static void main(final String... args) throws RemoteException {
        final BiFunction<Integer, String, String> getOrDefault = (index, orDefault) ->
                index >= args.length ? orDefault : args[index];

        final String firstName = getOrDefault.apply(0, "Alex");
        final String lastName = getOrDefault.apply(1, "Sin");
        final String passport = getOrDefault.apply(2, "12345");
        final String accountId = getOrDefault.apply(3, "alexsin");
        final int port = args.length > 0 ? Integer.parseInt(args[4]) : DEFAULT_PORT;

        final Bank bank;
        try {
            bank = (Bank) LocateRegistry.getRegistry(port).lookup("//localhost/bank");
        } catch (final NotBoundException e) {
            System.out.println("Bank is not bound");
            return;
        }

        Person person = bank.createPerson(firstName, lastName, passport);

        person = bank.findByPassport(person.passport(), true);
        Account account = bank.getAccount(accountId, person);

        if (account == null) {
            System.out.println("Creating account...");
            account = bank.createAccount(accountId, person);
        }

        System.out.println("Account id: " + account.getId());
        System.out.println("Money: " + account.getAmount());
        System.out.println("Adding money");
        account.setAmount(account.getAmount() + 100);
        System.out.println("Money: " + account.getAmount());
    }
}
