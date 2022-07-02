package info.kgeorgiy.ja.sinitsyn.bank.person;

import info.kgeorgiy.ja.sinitsyn.bank.account.Account;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Local person implementation for {@link Person}
 *
 * @author AlexSin
 */
public record LocalPerson(String firstName, String lastName, String passport, ConcurrentMap<String, Account> accounts)
        implements Person, Serializable {

    /**
     * LocalPerson constructor
     *
     * @param firstName person's first name
     * @param lastName  person's last name
     * @param passport  person's passport
     */
    public LocalPerson {}

    @SuppressWarnings("SimplifyStreamApiCallChains")
    public Set<Account> getAccounts() {
        return accounts.values().stream().collect(Collectors.toSet());
    }

    /**
     * Search for an account of this person by its id
     *
     * @param accId account id
     * @return requested account of this person
     *
     * @see Account
     */
    public Account getAccountById(final String accId) {
        return accounts.get(accId);
    }
}
