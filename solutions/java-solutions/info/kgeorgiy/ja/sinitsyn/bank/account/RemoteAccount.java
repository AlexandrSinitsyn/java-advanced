package info.kgeorgiy.ja.sinitsyn.bank.account;

import java.io.Serializable;

/**
 * Remote account implementation for {@link Account}
 *
 * @author AlexSin
 */
public final class RemoteAccount implements Account, Serializable {
    private final String id;
    private int amount;

    /**
     * RemoteAccount constructor
     *
     * @param id account's id
     */
    public RemoteAccount(final String id) {
        this.id = id;
        amount = 0;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public synchronized int getAmount() {
        System.out.println("Getting amount of money for account " + id);
        return amount;
    }

    @Override
    public synchronized void setAmount(final int amount) {
        System.out.println("Setting amount of money for account " + id);
        this.amount = amount;
    }
}
