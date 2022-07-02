interface iA<A> { iA<A> a(); }
interface iB<B1, B2> extends iA<iB<B1, B2>> {}
interface iC<C> extends iB<iA<C>, iB<C, iC<C>>> {}
public abstract class TestGenerics<T> implements iC<iB<T, T>> {}

// abstract class iA<A> { abstract iA<A> a(); }
// abstract class iB<B1, B2> extends iA<iB<B1, B2>> {}
// abstract class iC<C> extends iB<iA<C>, iB<C, iC<C>>> {}
// public abstract class TestMaxWierd<T> extends iC<iB<T, T>> {}
