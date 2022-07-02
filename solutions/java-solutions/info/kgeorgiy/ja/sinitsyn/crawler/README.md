## Web Crawler

Напишите потокобезопасный класс <code>WebCrawler</code>, который
будет рекурсивно обходить сайты.
<ol><li>
Класс <code>WebCrawler</code> должен иметь конструктор
<pre>
public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost)
</pre><ul><li><code>downloader</code> позволяет скачивать страницы и
извлекать из них ссылки;
</li><li><code>downloaders</code> &mdash; максимальное число
одновременно загружаемых страниц;
</li><li><code>extractors</code> &mdash; максимальное число страниц,
из которых одновременно извлекаются ссылки;
</li><li><code>perHost</code> &mdash; максимальное число страниц,
одновременно загружаемых c одного хоста.
Для определения хоста следует использовать
метод <code>getHost</code> класса
<code>URLUtils</code> из тестов.
</li></ul></li><li>
Класс <code>WebCrawler</code> должен реализовывать интерфейс
<code>Crawler</code><pre>
public interface Crawler extends AutoCloseable {
    Result download(String url, int depth);<br>
    void close();
}
</pre><ul><li>
Метод <code>download</code> должен
рекурсивно обходить страницы, начиная с указанного URL,
на указанную глубину и возвращать
список загруженных страниц и файлов.

Например, если глубина равна 1, то должна быть
загружена только указанная страница. Если глубина равна
2, то указанная страница и те страницы и файлы, на которые
она ссылается, и так далее.

Этот метод может вызываться параллельно в нескольких потоках.
</li><li>
    Загрузка и обработка страниц (извлечение ссылок)
    должна выполняться максимально параллельно,
    с учетом ограничений на число одновременно
    загружаемых страниц (в том числе с одного хоста)
    и страниц, с которых загружаются ссылки.
</li><li>
    Для распараллеливания разрешается создать
    до <code>downloaders + extractors</code>
    вспомогательных потоков.
</li><li>
    Загружать и/или извлекать ссылки из одной
    и той же страницы в рамках одного обхода
    (<code>download</code>) запрещается.
</li><li>
    Метод <code>close</code> должен завершать все
    вспомогательные потоки.
</li></ul></li><li>
Для загрузки страниц должен применяться <code>Downloader</code>,
передаваемый первым аргументом конструктора.
<pre>
public interface Downloader {
    public Document download(final String url) throws IOException;
}
</pre><ul><li>
Метод <code>download</code> загружает документ по его адресу
(<a href="http://tools.ietf.org/html/rfc3986">URL</a>).
</li><li>
Документ позволяет получить ссылки по загруженной странице:
<pre>
public interface Document {
    List&lt;String&gt; extractLinks() throws IOException;
}
</pre>
Ссылки, возвращаемые документом, являются абсолютными
и имеют схему <code>http</code> или <code>https</code>.
</li></ul></li><li>
Должен быть реализован метод <code>main</code>,
позволяющий запустить обход из командной строки
<ul><li>
Командная строка
<pre>
WebCrawler url [depth [downloads [extractors [perHost]]]]
</pre></li><li>
Для загрузки страниц требуется использовать реализацию
<code>CachingDownloader</code> из тестов.
</li></ul></li></ol></li><li>
Версии задания
<ol><li><em>Простая</em> &mdash; не требуется учитывать ограничения
на число одновременных закачек с одного хоста
(<code>perHost &gt;= downloaders</code>).
</li><li><em>Полная</em> &mdash; требуется учитывать все ограничения.
</li><li><em>Бонусная</em> &mdash; сделать параллельный обход в ширину.
</li></ol></li><li>
Задание подразумевает активное использование Concurrency Utilities,
в частности, в решении не должно быть &laquo;велосипедов&raquo;,
аналогичных/легко сводящихся к классам из Concurrency Utilities.
