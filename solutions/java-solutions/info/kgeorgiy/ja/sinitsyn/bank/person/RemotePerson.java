package info.kgeorgiy.ja.sinitsyn.bank.person;

/**
 * Remote person implementation for {@link Person}
 *
 * @author AlexSin
 */
public record RemotePerson(String firstName, String lastName, String passport) implements Person {

    /**
     * RemotePerson constructor
     *
     * @param firstName person's first name
     * @param lastName person's last name
     * @param passport person's passport
     */
    public RemotePerson {}
}
