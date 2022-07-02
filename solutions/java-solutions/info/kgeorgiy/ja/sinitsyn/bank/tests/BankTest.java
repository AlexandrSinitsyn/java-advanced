package info.kgeorgiy.ja.sinitsyn.bank.tests;

import info.kgeorgiy.ja.sinitsyn.bank.account.Account;
import info.kgeorgiy.ja.sinitsyn.bank.bank.Bank;
import info.kgeorgiy.ja.sinitsyn.bank.bank.RemoteBank;
import info.kgeorgiy.ja.sinitsyn.bank.person.Person;
import org.junit.*;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;

/**
 * Tests for client-server application for bank
 *
 * @author AlexSin
 */
@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class BankTest {

    /** method to run this class as a self-sufficient program. No arguments are required */
    public static void main(final String[] args) {
        final var junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        final Result result = junit.run(BankTest.class);

        if (result.wasSuccessful()) {
            System.exit(0);
        } else {
            System.exit(1);
        }
    }

    private static final String BANK_HOST = "//localhost/bank";
    private static final int PORT = 8888;

    private static Bank bank;

    @RunWith(JUnit4.class)
    public static final class BankHostBreakingTest {
        @Test
        public void noRegistry() {
            try {
                final Bank bank = new RemoteBank(PORT);
                UnicastRemoteObject.exportObject(bank, PORT);
                java.rmi.Naming.rebind(BANK_HOST, bank);

                Assert.fail();
            } catch (final Exception ignored) {}
        }
    }

    @BeforeClass
    public static void bankHostBreakingTest() {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        junit.run(BankHostBreakingTest.class);
    }

    @BeforeClass
    public static void startServer() {
        try {
            final Registry registry = LocateRegistry.createRegistry(PORT);

            final Bank bank = new RemoteBank(PORT);
            UnicastRemoteObject.exportObject(bank, PORT);
            registry.rebind(BANK_HOST, bank);

            BankTest.bank = (Bank) LocateRegistry.getRegistry(PORT).lookup(BANK_HOST);
        } catch (final RemoteException | NotBoundException e) {
            Assert.fail(e.getMessage());
        }
    }

    @AfterClass
    public static void stopServer() {
        try {
            LocateRegistry.getRegistry(PORT).unbind(BANK_HOST);
        } catch (final RemoteException | NotBoundException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void client() throws RemoteException {
        final Person created = bank.createPerson("A", "A", "111");

        Assert.assertNotNull("Expected person", created);
        Assert.assertEquals("Expected same names for new person", "A", created.firstName());
        Assert.assertEquals("Expected same names for new person", "A", created.lastName());
        Assert.assertEquals("Expected same names for new person", "111", created.passport());

        final Person person = bank.findByPassport("111", false);

        Assert.assertNotNull("Expected person", person);
        Assert.assertEquals("Expected same names for new person", person.firstName(), created.firstName());
        Assert.assertEquals("Expected same names for new person", person.lastName(), created.lastName());
        Assert.assertEquals("Expected same names for new person", person.passport(), created.passport());
    }

    @Test
    public void twoClients() throws RemoteException {
        bank.createPerson("A", "A", "111");
        bank.createPerson("B", "B", "222");

        final Person a = bank.findByPassport("111", false);
        final Person b = bank.findByPassport("222", false);

        Assert.assertNotNull("Expected person", a);
        Assert.assertNotNull("Expected person", b);
        Assert.assertNotEquals("Expected different persons", a, b);
    }

    @Test
    public void account() throws RemoteException {
        final Person person = bank.createPerson("A", "A", "111");
        final Account created = bank.createAccount("B", person);

        Assert.assertNotNull("Expected account", created);
        Assert.assertEquals("Expected same ids for new account", Account.getBankIdInterpretation("B", person), created.getId());
        Assert.assertEquals("Expected same amounts for new account", 0, created.getAmount());

        final Account account = bank.getAccount("B", person);

        Assert.assertNotNull("Expected account", account);
        Assert.assertEquals("Expected same ids for new account", created.getId(), account.getId());
        Assert.assertEquals("Expected same amounts for new account", created.getAmount(), account.getAmount());

        final Set<Account> gotten = bank.getAccountsOfPerson(person);

        Assert.assertNotNull("Expected set of accounts", gotten);
        Assert.assertEquals("Expected only one account", 1, gotten.size());
        Assert.assertEquals("Expected created account in set of accounts", created, gotten.iterator().next());
    }

    @Test
    @SuppressWarnings("SimplifiableAssertion")
    public void twoAccounts() throws RemoteException {
        final Person person = bank.createPerson("P", "P", "111");
        final Account a = bank.createAccount("A", person);
        final Account b = bank.createAccount("B", person);

        final Set<Account> gotten = bank.getAccountsOfPerson(person);

        Assert.assertEquals("Expected only two account for person", 2, gotten.size());

        final var it = gotten.iterator();

        final Account first = it.next();
        if (first.equals(a)) {
            Assert.assertTrue("Expected accounts to be equals", b.equals(it.next()));
        } else {
            Assert.assertTrue("Expected accounts to be equals", b.equals(first) && a.equals(it.next()));
        }
    }

    @Test
    @SuppressWarnings("SimplifiableAssertion")
    public void twoAccountsForTwoClients() throws RemoteException {
        final Person p1 = bank.createPerson("P1", "P1", "111");
        final Person p2 = bank.createPerson("P2", "P2", "222");

        final Account a1 = bank.createAccount("A1", p1);
        final Account a2 = bank.createAccount("A2", p2);

        final Account $a2$ = bank.getAccountsOfPerson(p2).iterator().next();

        Assert.assertEquals("Expected right id for account", Account.getBankIdInterpretation("A1", p1), a1.getId());
        Assert.assertEquals("Expected right id for account", Account.getBankIdInterpretation("A2", p2), a2.getId());
        Assert.assertTrue("Expected right id for account", a2.equals($a2$));
    }

    @Test
    public void updateAmount() throws RemoteException {
        final Person p = bank.createPerson("P", "P", "111");
        final Account a = bank.createAccount("A", p);

        final int newAmount = a.getAmount() + 100;

        a.setAmount(newAmount);

        Assert.assertEquals("Expected change in the account's amount of money", newAmount, bank.getAccount("A", p).getAmount());
    }

    @Test
    public void localUpdate() throws RemoteException {
        Person p = bank.createPerson("P", "P", "111");
        final Account a = bank.createAccount("A", p);
        a.setAmount(100);

        p = bank.findByPassport("111", true);
        a.setAmount(400);

        Assert.assertEquals("Expected no changes from a local person", 100, bank.getAccount("A", p).getAmount());
    }
}
