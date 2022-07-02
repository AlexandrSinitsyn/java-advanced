package info.kgeorgiy.ja.sinitsyn.bank.account;

import info.kgeorgiy.ja.sinitsyn.bank.person.Person;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote account interface
 *
 * @author AlexSin
 */
public interface Account extends Remote {
    /** Returns account identifier. */
    String getId() throws RemoteException;

    /** Returns amount of money at the account. */
    int getAmount() throws RemoteException;

    /** Sets amount of money at the account. */
    void setAmount(int amount) throws RemoteException;

    /**
     * Returns bank representation of account's id
     * @param id imaginary account's id
     * @param person person to whom this account belongs
     * @return real account's id
     * @throws RemoteException if remote object is inaccessible
     *
     * @see Person
     */
    static String getBankIdInterpretation(final String id, final Person person) throws RemoteException {
        return person.passport() + ":" + id;
    }
}