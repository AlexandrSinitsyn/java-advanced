package info.kgeorgiy.ja.sinitsyn.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation for {@link ListIP}
 *
 * @author AlexSin
 *
 * @see ListIP
 */
public class IterativeParallelism implements ListIP {

    private ParallelMapper parallelMapper;

    /**
     * Default constructor. Calling it means that for each parallel operation there will
     * be created separated threads. And they will die after operations on given list
     * will be done.
     */
    public IterativeParallelism() {}

    /**
     * Constructor of {@link ParallelMapper}. After calling this constructor all your
     * requests will delegate to this parallel mapper. It means that all operations
     * will be done by the same range of threads, and they will die only after calling
     * {@link ParallelMapper#close()}.
     *
     * @param parallelMapper tool that generates range of threads that will solve
     *                       all the queue of tasks.
     *
     * @see ParallelMapper
     */
    public IterativeParallelism(final ParallelMapper parallelMapper) {
        this.parallelMapper = parallelMapper;
    }

    /**
     * Searches for the maximum value in the list. Searching will be done in separated
     * threads. Number of threads is specified by value "{@code threads}". If there is
     * no elements in the given list then throws {@link NoSuchElementException}.
     *
     * @param threads number or concurrent threads.
     * @param values values to get maximum of.
     * @param comparator value comparator.
     * @param <T> value type.
     *
     * @return maximum of given values
     *
     * @throws InterruptedException if executing thread was interrupted.
     * @throws NoSuchElementException if no values are given.
     */
    @Override
    public <T> T maximum(final int threads, final List<? extends T> values, final Comparator<? super T> comparator) throws InterruptedException {
        return nonNullThreading(threads, values, vls -> vls.max(comparator).orElse(null)).stream()
                .max(comparator).orElseThrow(() -> new NoSuchElementException("Collection is empty"));
    }

    /**
     * Searches for the minimum value in the list. Searching will be done in separated
     * threads. Number of threads is specified by value "{@code threads}". If there is
     * no elements in the given list then throws {@link NoSuchElementException}.
     *
     * @param threads number or concurrent threads.
     * @param values values to get minimum of.
     * @param comparator value comparator.
     * @param <T> value type.
     *
     * @return minimum of given values
     *
     * @throws InterruptedException if executing thread was interrupted.
     * @throws NoSuchElementException if no values are given.
     */
    @Override
    public <T> T minimum(final int threads, final List<? extends T> values, final Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, comparator.reversed());
    }

    /**
     * Checks if all elements from the collection satisfy the given predicate.
     * All operations will be done in separated threads. Number of threads
     * is specified by value "{@code threads}". If there is no elements in
     * the given list then returns {@code true}.
     *
     * @param threads number or concurrent threads.
     * @param values values to test.
     * @param predicate test predicate.
     * @param <T> value type.
     *
     * @return whether all values satisfy predicate or {@code true}, if no values are given
     *
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> boolean all(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        return threading(threads, values, vls -> vls.allMatch(predicate)).stream().allMatch(b -> b);
    }

    /**
     * Checks if any element from the collection satisfies the given predicate.
     * All operations will be done in separated threads. Number of threads
     * is specified by value "{@code threads}". If there is no elements in
     * the given list then returns {@code false}.
     *
     * @param threads number or concurrent threads.
     * @param values values to test.
     * @param predicate test predicate.
     * @param <T> value type.
     *
     * @return whether any value satisfies predicate or {@code false}, if no values are given
     *
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> boolean any(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, predicate.negate());
    }

    /**
     * Joins all the elements to one string using their {@code toString} method
     * to convert to each item to char sequence. All operations will be done in
     * separated threads. Number of threads is specified by value "{@code threads}".
     * If there is no elements in the given list then returns {@code empty string}.
     *
     * @param threads number of concurrent threads.
     * @param values values to join.
     *
     * @return list of joined result of {@link #toString()} call on each value.
     *
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public String join(final int threads, final List<?> values) throws InterruptedException {
        return String.join("", threading(threads, values,
                vls -> vls.filter(Objects::nonNull).map(Object::toString).collect(Collectors.joining())));
    }

    /**
     * Iterates through all elements in the collection and removes all of them that
     * do not satisfy the given predicate. All operations will be done in
     * separated threads. Number of threads is specified by value "{@code threads}".
     * If there is no elements in the given list then returns {@code empty list}.
     *
     * @param threads number of concurrent threads.
     * @param values values to filter.
     * @param predicate filter predicate.
     *
     * @return list of values satisfying given predicated. Order of values is preserved.
     *
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> List<T> filter(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        return applyToAll(threads, values, predicate, e -> e);
    }

    /**
     * Iterates through all elements in the collection and maps all of them by
     * given function. All operations will be done in separated threads. Number
     * of threads is specified by value "{@code threads}". If there is no elements
     * in the given list then returns {@code empty list}.
     *
     * @param threads number of concurrent threads.
     * @param values values to map.
     * @param f mapper function.
     *
     * @return list of values mapped by given function.
     *
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T, U> List<U> map(final int threads, final List<? extends T> values, final Function<? super T, ? extends U> f) throws InterruptedException {
        return applyToAll(threads, values, e -> true, f);
    }

    private <T, U> List<U> applyToAll(final int threads, final List<? extends T> values,
                                      final Predicate<? super T> predicate,
                                      final Function<? super T, ? extends U> fun) throws InterruptedException {
        return threading(threads, values, vls -> vls.filter(predicate).map(fun).toList()).stream()
                .<U>flatMap(Collection::stream).toList();
    }

    private <T, R> List<R> threading(final int threads, final List<T> values, final Function<Stream<T>, R> fun) throws InterruptedException {
        assert threads > 0;

        final var thr = Math.min(threads, values.size());

        final var sizeByThread = values.size() / thr;
        var left = values.size() % thr;

        var index = 0;

        final List<Stream<T>> listOfParts = new ArrayList<>();
        for (int i = 0; i < thr; i++) {
            final var start = index;
            final int size = sizeByThread + (left-- > 0 ? 1 : 0);
            index += size;

            listOfParts.add(values.subList(start, start + size).stream());
        }

        if (parallelMapper != null) {
            return parallelMapper.map(fun, listOfParts);
        }

        final List<R> res = new ArrayList<>(Collections.nCopies(thr, null));
        final List<Thread> listOfThreads = new ArrayList<>();

        for (int i = 0; i < thr; i++) {
            final var ind = i;
            final var thread = new Thread(() -> res.set(ind, fun.apply(listOfParts.get(ind))));

            thread.start();

            listOfThreads.add(thread);
        }

        final var failed = new InterruptedException();
        for (final Thread thread : listOfThreads) {
            assert thread != null;

            try {
                thread.join();
            } catch (final InterruptedException e) {
                failed.addSuppressed(e);
            }
        }

        if (failed.getSuppressed().length != 0) {
            throw failed;
        }

        return res;
    }

    private <T, R> List<R> nonNullThreading(final int threads, final List<T> values, final Function<Stream<T>, R> fun) throws InterruptedException {
        if (values.isEmpty()) {
            throw new NoSuchElementException("Collection is empty");
        }

        return threading(threads, values, fun);
    }
}
