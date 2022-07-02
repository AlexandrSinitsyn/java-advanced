package info.kgeorgiy.ja.sinitsyn.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

/**
 * Threadsafe implementation for {@link Crawler}. A tool for crawling across websites and
 * their links (in bfs) and downloads all of them with the given {@link Downloader}.
 *
 * @author AlexSin
 * @see Crawler
 */
public final class WebCrawler implements Crawler, AdvancedCrawler {

    private static final class WebCrawlerException extends RuntimeException {

        public WebCrawlerException(final String message) {
            super(message);
        }

        public WebCrawlerException(final String message, final Throwable throwable) {
            super(message, throwable);
        }
    }

    private final class HostCrawler {

        private final ConcurrentMap<String, Semaphore> hostMap;

        public HostCrawler() {
            hostMap = new ConcurrentHashMap<>();
        }

        public void download(final Phaser phaser, final String url, final Predicate<String> isHostAcceptable,
                             final Set<String> allUrls, final Set<String> accepted,
                             final Set<String> errors, final Map<String, IOException> errorsAccepted, final Set<String> toVisit) {
            if (allUrls.contains(url) || errors.contains(url)) {
                return;
            }

            phaser.register();

            downloaders.submit(() -> {
                boolean isAcceptable = true;
                try {
                    final var host = hostMap.computeIfAbsent(URLUtils.getHost(url), ignored -> new Semaphore(perHost));

                    isAcceptable = isHostAcceptable.test(URLUtils.getHost(url));

                    final Document document;
                    try {
                        host.acquire();

                        document = downloader.download(url);
                    } finally {
                        host.release();
                    }

                    allUrls.add(url);
                    if (isAcceptable) {
                        accepted.add(url);
                    }

                    extractors.submit(extractorTask(phaser, url, document, isAcceptable, errors, errorsAccepted, toVisit));
                } catch (final IOException e) {
                    errors.add(url);
                    if (isAcceptable) {
                        errorsAccepted.put(url, e);
                    }
                } catch (final InterruptedException e) {
                    throwException("Interrupted while waiting for permission to the host", e);
                } finally {
                    phaser.arrive();
                }
            });
        }

        public Runnable extractorTask(final Phaser phaser, final String url, final Document document, final boolean isAcceptable,
                                      final Set<String> errors, final Map<String, IOException> errorsAccepted, final Set<String> toVisit) {
            phaser.register();

            return () -> {
                try {
                    toVisit.addAll(document.extractLinks());
                } catch (final IOException e) {
                    errors.add(url);
                    if (isAcceptable) {
                        errorsAccepted.put(url, e);
                    }
                } finally {
                    phaser.arrive();
                }
            };
        }
    }

    private final ExecutorService downloaders;
    private final ExecutorService extractors;
    private final Downloader downloader;
    private final int perHost;

    /**
     * WebCrawler constructor. Creates an instance of threadsafe {@link WebCrawler}
     * and initializes two thread pools: {@link #downloaders} and {@link #extractors}.
     *
     * @param downloader allows download website's pages and extract links from them
     * @param downloaders the maximum number of pages that could be downloading in parallel.
     *                    To prevent a huge amount of threads pinching a pure site
     * @param extractors the maximum number of pages from which links will be extracted in parallel.
     *                   To prevent overloading of the processor while a huge amount of threads
     *                   is extracting links in parallel
     * @param perHost the maximum number of pages that could be downloaded in parallel from one Host.
     *                To prevent the overloading of the host
     *
     * @see Downloader
     * @see ExecutorService
     */
    public WebCrawler(final Downloader downloader, final int downloaders, final int extractors, final int perHost) {
        this.downloaders = Executors.newFixedThreadPool(downloaders);
        this.extractors = Executors.newFixedThreadPool(extractors);

        this.downloader = downloader;
        this.perHost = perHost;
    }

    private static void throwException(final String message, final Throwable exception) {
        throw new WebCrawlerException(message, exception);
    }

    private static void throwException(final boolean condition, final String message) {
        if (condition) {
            throw new WebCrawlerException(message);
        }
    }

    /**
     * Main method to run {@link WebCrawler} as a self-sufficient program
     *
     * @param args argument from console. You can pass from one to five arguments as this:
     *             <ul>
     *             <li>url - The url to start crawling the internet</li>
     *             <li>[optional] depth - the maximum depth of the crawling</li>
     *             <li>[optional] downloads - the number of {@link #downloaders}</li>
     *             <li>[optional] extractors - the number of {@link #extractors}</li>
     *             <li>[optional] perHost - the maximum count of threads working on one host</li>
     *             </ul>
     *
     * @see #WebCrawler(Downloader, int, int, int)
     */
    public static void main(final String[] args) {
        throwException(args == null, "Args can not be null");
        throwException(Arrays.stream(args).anyMatch(Objects::isNull), "Args can not be null");
        throwException(args.length < 1, "No less than 1 argument can be passed");
        throwException(args.length > 5, "Maximum 5 arguments can be passed");

        final var url = args[0];

        final ToIntFunction<String> parseInt = s -> {
            try {
                return Integer.parseInt(s);
            } catch (final NumberFormatException e) {
                throwException("Number should be passed", e);
                return -1;
            }
        };

        final ToIntBiFunction<Integer, Integer> getOrDefault = (index, orDefault) ->
                index >= args.length ? orDefault : parseInt.applyAsInt(args[index]);

        final var depth = getOrDefault.applyAsInt(1, 3);
        final var downloads = getOrDefault.applyAsInt(2, 10);
        final var extractors = getOrDefault.applyAsInt(3, 10);
        final var perHost = getOrDefault.applyAsInt(4, 10);

        try {
            final var crawler = new WebCrawler(new CachingDownloader(), downloads, extractors, perHost);

            crawler.download(url, depth);
        } catch (final IOException e) {
            throwException("Fail occurred while downloading website", e);
        }
    }

    @Override
    public Result download(final String url, final int depth) {
        return download(url, depth, ignored -> true);
    }

    @Override
    public Result download(final String url, final int depth, final List<String> hosts) {
        //noinspection SimplifyStreamApiCallChains
        final var hostSet = hosts.stream().collect(Collectors.toSet());
        return download(url, depth, hostSet::contains);
    }

    private Result download(final String url, final int depth, final Predicate<String> isHostAcceptable) {
        final Set<String> urls = ConcurrentHashMap.newKeySet();
        final Set<String> accepted = ConcurrentHashMap.newKeySet();

        final Set<String> errors = ConcurrentHashMap.newKeySet();

        final Map<String, IOException> errorsAccepted = new ConcurrentHashMap<>();

        Set<String> nextLevel = ConcurrentHashMap.newKeySet();
        final Set<String> currentLevel = ConcurrentHashMap.newKeySet();
        currentLevel.add(url);

        final var hostCrawler = new HostCrawler();
        for (int i = 0; i < depth; i++) {
            final var phaser = new Phaser(1);

            for (final var link : currentLevel) {
                hostCrawler.download(phaser, link, isHostAcceptable, urls, accepted, errors, errorsAccepted, nextLevel);
            }

            phaser.arriveAndAwaitAdvance();

            currentLevel.clear();
            currentLevel.addAll(nextLevel);
            nextLevel.clear();
        }

        return new Result(new ArrayList<>(accepted), errorsAccepted);
    }

    @Override
    public void close() {
        downloaders.shutdownNow();
        extractors.shutdownNow();

        throwException(!downloaders.isShutdown() && !extractors.isShutdown(), "Executor service are not shutdown");
    }
}
