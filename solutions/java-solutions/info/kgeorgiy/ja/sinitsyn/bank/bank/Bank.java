package info.kgeorgiy.ja.sinitsyn.bank.bank;

import info.kgeorgiy.ja.sinitsyn.bank.account.Account;
import info.kgeorgiy.ja.sinitsyn.bank.person.Person;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

/**
 * Remote bank interface
 *
 * @author AlexSin
 */
public interface Bank extends Remote {
    /**
     * Creates a new account with specified identifier if it is not already exists.
     * @param id account id
     * @param person a person to create an account for
     * @return created or existing account.
     *
     * @see Account
     */
    Account createAccount(final String id, final Person person) throws RemoteException;

    /**
     * Returns account by identifier.
     * @param id account id
     * @param person person to know whose account look for
     * @return account with specified identifier or {@code null} if such account does not exist.
     * @throws RemoteException if remote object is untouchable (no way to get access to it)
     *
     * @see Account
     */
    Account getAccount(final String id, final Person person) throws RemoteException;

    /**
     * Creates a person unless one with such passport already exists
     *
     * @param firstName person's name
     * @param lastName person's last name
     * @param passport person's passport
     * @return saved person for given {@code passport}: it will return new if there was no person
     * before and previously saved if there was
     * @throws RemoteException if remote bank is inaccessible
     *
     * @see Person
     */
    Person createPerson(final String firstName, final String lastName, final String passport) throws RemoteException;

    /**
     * Looks up for a person with such passport
     *
     * @param passport person's passport to look by
     * @param local flag to choose between {@link info.kgeorgiy.ja.sinitsyn.bank.person.LocalPerson} and
     *              {@link info.kgeorgiy.ja.sinitsyn.bank.person.RemotePerson} to return
     * @return {@link info.kgeorgiy.ja.sinitsyn.bank.person.LocalPerson} if {@code local = true} and person exists,
     * {@link info.kgeorgiy.ja.sinitsyn.bank.person.RemotePerson} if {@code local = false} and person exists
     * and {@code null} otherwise
     * @throws RemoteException if remote object is inaccessible
     *
     * @see Person
     */
    Person findByPassport(final String passport, final boolean local) throws RemoteException;

    /**
     * Return all person's accounts
     *
     * @param person person whose accounts return
     * @return person's accounts
     * @throws RemoteException if remote object is inaccessible
     *
     * @see Account
     * @see Person
     */
    Set<Account> getAccountsOfPerson(final Person person) throws RemoteException;
}
