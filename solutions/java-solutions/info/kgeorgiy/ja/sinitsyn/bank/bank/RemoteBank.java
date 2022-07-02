package info.kgeorgiy.ja.sinitsyn.bank.bank;

import info.kgeorgiy.ja.sinitsyn.bank.account.Account;
import info.kgeorgiy.ja.sinitsyn.bank.account.RemoteAccount;
import info.kgeorgiy.ja.sinitsyn.bank.person.LocalPerson;
import info.kgeorgiy.ja.sinitsyn.bank.person.Person;
import info.kgeorgiy.ja.sinitsyn.bank.person.RemotePerson;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Remote bank implementation for {@link Bank}
 *
 * @author AlexSin
 */
public final class RemoteBank implements Bank {
    private final int port;
    private final ConcurrentMap<String, Account> accounts;
    private final ConcurrentMap<String, Person> persons;
    private final ConcurrentMap<String, ConcurrentMap<String, Account>> personsToAccountsMap;

    /**
     * RemoteBank constructor
     *
     * @param port to host this bank on
     */
    public RemoteBank(final int port) {
        this.port = port;
        accounts = new ConcurrentHashMap<>();
        persons = new ConcurrentHashMap<>();
        personsToAccountsMap = new ConcurrentHashMap<>();
    }

    @Override
    public Account createAccount(final String id, final Person person) throws RemoteException {
        System.out.printf("Creating account %s for person [%s %s] on port %d\n",
                id, person.firstName(), person.lastName(), port);

        final String accId = Account.getBankIdInterpretation(id, person);
        final var account = new RemoteAccount(accId);

        if (accounts.putIfAbsent(accId, account) == null) {
            personsToAccountsMap.get(person.passport()).put(accId, account);
            UnicastRemoteObject.exportObject(accounts.get(accId), port);
        }

        return accounts.get(accId);
    }

    @Override
    public Account getAccount(final String id, final Person person) throws RemoteException {
        final String accId = Account.getBankIdInterpretation(id, person);

        System.out.println("Retrieving account " + accId);

        final Account account = accounts.get(accId);
        return person instanceof final LocalPerson localPerson ?
                localPerson.getAccountById(accId) : account;
    }

    @Override
    public Person createPerson(final String firstName, final String lastName, final String passport) throws RemoteException {
        final RemotePerson person = new RemotePerson(firstName, lastName, passport);

        if (persons.putIfAbsent(passport, person) == null) {
            personsToAccountsMap.putIfAbsent(passport, new ConcurrentHashMap<>());
            UnicastRemoteObject.exportObject(persons.get(passport), port);

            System.out.println("Person created");
        }

        return persons.get(passport);
    }

    @Override
    public Person findByPassport(final String passport, final boolean local) throws RemoteException {
        final Person person = persons.get(passport);

        if (local) {
            final Function<Account, Account> copyAccount = account -> {
                RemoteAccount res = null;

                try {
                    res = new RemoteAccount(account.getId());
                    res.setAmount(account.getAmount());
                } catch (final RemoteException e) {
                    System.out.println("Failed to copy remote account for local person with passport '" + passport + "'");
                }

                return res;
            };

            return new LocalPerson(person.firstName(), person.lastName(), person.passport(),
                    personsToAccountsMap.get(person.passport()).entrySet()
                            .stream().collect(ConcurrentHashMap::new,
                                    (m, e) -> m.put(e.getKey(), copyAccount.apply(e.getValue())), ConcurrentHashMap::putAll));
        }

        return person;
    }

    @Override
    @SuppressWarnings("SimplifyStreamApiCallChains")
    public Set<Account> getAccountsOfPerson(final Person person) throws RemoteException {
        return person instanceof final LocalPerson localPerson ?
                localPerson.getAccounts() : personsToAccountsMap.get(person.passport())
                .values().stream().collect(Collectors.toSet());
    }
}
