package info.kgeorgiy.ja.sinitsyn.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Implementation for {@link ParallelMapper}
 *
 * @author AlexSin
 * @see ParallelMapper
 */
public class ParallelMapperImpl implements ParallelMapper {

    private final List<Thread> threads;
    private final Queue<Runnable> tasks;

    /**
     * Constructor to generated range of threads to solve the tasks
     * that will be added to {@link #tasks} queue after each invoke
     * of the {@link #map(Function, List)}method.
     *
     * @param threads count of threads to solve tasks
     */
    public ParallelMapperImpl(final int threads) {
        tasks = new ArrayDeque<>();

        this.threads = IntStream.range(0, threads).mapToObj(i -> {
            final var thread = new Thread(() -> {
                try {
                    while (!Thread.interrupted()) {
                        final Runnable task;

                        synchronized (tasks) {
                            while (tasks.isEmpty()) {
                                tasks.wait();
                            }

                            task = tasks.poll();

                            tasks.notify();
                        }

                        task.run();
                    }
                } catch (InterruptedException e) {
                    // do nothing
                } finally {
                    Thread.currentThread().interrupt();
                }
            });

            thread.start();

            return thread;
        }).toList();
    }

    private static final class ThreadingWrapper<E> {

        private final E[] arr;
        private int cd;

        @SuppressWarnings("unchecked")
        public ThreadingWrapper(final int size) {
            cd = size;
            arr = (E[]) new Object[size];
        }

        public synchronized void done(final int index, final E e) {
            arr[index] = e;

            if (--cd <= 0) {
                notify();
            }
        }

        public synchronized E[] getArr() throws InterruptedException {
            while (cd > 0) {
                wait();
            }

            return arr;
        }
    }

    @Override
    public <T, R> List<R> map(final Function<? super T, ? extends R> f, final List<? extends T> args) throws InterruptedException {
        final var tw = new ThreadingWrapper<R>(args.size());

        synchronized (tasks) {
            IntStream.range(0, args.size())
                    .mapToObj(i -> Map.entry(i, args.get(i)))
                    .forEach(ew -> tasks.add(() -> tw.done(ew.getKey(), f.apply(ew.getValue()))));
            tasks.notify();
        }

        return Arrays.asList(tw.getArr());
    }

    @Override
    public void close() {
        threads.forEach(t -> {
            t.interrupt();

            while (t.isAlive()) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
        });
    }
}