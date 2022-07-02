## Итеративный параллелизм

<ol><li>
    Реализуйте класс <tt>IterativeParallelism</tt>,
    который будет обрабатывать списки в несколько потоков.
</li><li>
    В простом варианте должны быть реализованы следующие методы:
    <ul><li><tt>minimum(threads, list, comparator)</tt> —
            первый минимум;
        </li><li><tt>maximum(threads, list, comparator)</tt> —
            первый максимум;
        </li><li><tt>all(threads, list, predicate)</tt> —
            проверка, что все элементы списка, удовлетворяют предикату;                    
        </li><li><tt>any(threads, list, predicate)</tt> —
            проверка, что существует элемент списка, удовлетворяющий предикату.
        </li></ul></li><li>
    В сложном варианте должны быть дополнительно реализованы следующие методы:
    <ul><li><tt>filter(threads, list, predicate)</tt> —
            вернуть список, содержащий элементы удовлетворяющие предикату;
        </li><li><tt>map(threads, list, function)</tt> —
            вернуть список, содержащий результаты применения функции;
        </li><li><tt>join(threads, list)</tt> —
            конкатенация строковых представлений элементов списка.
        </li></ul></li><li>
    Во все функции передается параметр <tt>threads</tt> —
    сколько потоков надо использовать при вычислении.
    Вы можете рассчитывать, что число потоков относительно мало.
</li><li>
    Не следует рассчитывать на то, что переданные компараторы,
    предикаты и функции работают быстро.
</li><li>
    При выполнении задания <strong>нельзя</strong> использовать
    <i>Concurrency Utilities</i>.
</li><li>
    Рекомендуется подумать, какое отношение к
    заданию имеют <a href="https://en.wikipedia.org/wiki/Monoid">моноиды</a>.
</li></ol>

---

## Параллельный запуск

<ol><li>
            Напишите класс <tt>ParallelMapperImpl</tt>, реализующий интерфейс
            <tt>ParallelMapper</tt>.
<pre>public interface ParallelMapper extends AutoCloseable {
    &lt;T, R&gt; List&lt;R&gt; map(
        Function&lt;? super T, ? extends R&gt; f,
        List&lt;? extends T&gt; args
    ) throws InterruptedException;<br>
    @Override
    void close();
}
</pre><ul><li>
        Метод <tt>run</tt> должен параллельно вычислять
        функцию <tt>f</tt> на каждом из указанных аргументов
        (<tt>args</tt>).
    </li><li>
        Метод <tt>close</tt> должен останавливать все рабочие потоки.
    </li><li>
        Конструктор <tt>ParallelMapperImpl(int threads)</tt>
        создает <tt>threads</tt> рабочих потоков, которые могут
        быть использованы для распараллеливания.
    </li><li>
        К одному <tt>ParallelMapperImpl</tt> могут одновременно обращаться
        несколько клиентов.
    </li><li>
        Задания на исполнение должны накапливаться в очереди и обрабатываться
        в порядке поступления.
    </li><li>
        В реализации не должно быть активных ожиданий.
    </li></ul></li><li>
Доработайте класс <tt>IterativeParallelism</tt> так,
чтобы он мог использовать <tt>ParallelMapper</tt>.
<ul><li>
        Добавьте конструктор <tt>IterativeParallelism(ParallelMapper)</tt></li><li>
        Методы класса должны делить работу на <tt>threads</tt>
        фрагментов и исполнять их при помощи <tt>ParallelMapper</tt>.
    </li><li>
        При наличии <tt>ParallelMapper</tt> сам
        <tt>IterativeParallelism</tt> новые потоки создавать не должен.
    </li><li>
        Должна быть возможность одновременного запуска и работы
        нескольких клиентов, использующих один <tt>ParallelMapper</tt>.
    </li></ul></li></ol>
