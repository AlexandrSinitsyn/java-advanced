package info.kgeorgiy.ja.sinitsyn.bank.person;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote person interface
 *
 * @author AlexSin
 */
public interface Person extends Remote {
    /** Returns person's first name*/
    String firstName() throws RemoteException;

    /** Returns person's last name */
    String lastName() throws RemoteException;

    /** Returns person's passport */
    String passport() throws RemoteException;
}
