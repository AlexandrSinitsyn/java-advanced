/**
 * Base classes to implement for
 * <a href="https://www.kgeorgiy.info/courses/java-advanced/">Java Advanced</a> course.
 *
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
open module base {
    requires java.management;
    requires java.management.rmi;
    requires java.compiler;
    requires java.sql;
    requires java.sql.rowset;
    requires java.desktop;

    requires jsoup;

    exports info.kgeorgiy.java.advanced.student;
    exports info.kgeorgiy.java.advanced.implementor;
    exports info.kgeorgiy.java.advanced.concurrent;
    exports info.kgeorgiy.java.advanced.mapper;
    exports info.kgeorgiy.java.advanced.crawler;
}
