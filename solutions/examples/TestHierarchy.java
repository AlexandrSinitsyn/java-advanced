import java.util.Set;

final class Def {
    static abstract class B<A, B extends Exception, Q extends B> {
        abstract A a() throws B;
        abstract Q q() throws Q;
    }
}

abstract class A<B, Q extends Exception> extends Def.B<B, Q, Q> {
    abstract B get() throws Q;
}

public abstract class TestHierarchy<Q extends Exception> extends A<Set<Q>, Q> {
    abstract <B extends Set<B>, C extends Number & Comparable<Q>> void testMethod(B a, C b) throws Q;
}
