/**
 * Solutions' module for <b>java-advanced</b> tasks
 *
 * @author AlexSin
 */
open module info.kgeorgiy.ja.sinitsyn {
    requires transitive base;

    requires java.compiler;
    requires java.rmi;
    requires jdk.httpserver;

    requires junit;

    exports info.kgeorgiy.ja.sinitsyn.implementor;

    exports info.kgeorgiy.ja.sinitsyn.bank.bank;
    exports info.kgeorgiy.ja.sinitsyn.bank.account;
    exports info.kgeorgiy.ja.sinitsyn.bank.person;

    exports info.kgeorgiy.ja.sinitsyn.bank.tests
            to junit;

    exports info.kgeorgiy.ja.sinitsyn.i18n.stats;
    exports info.kgeorgiy.ja.sinitsyn.i18n
            to junit;
}