package arrayset;

import java.util.*;

@SuppressWarnings("unchecked")
public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E> {

    private final ReversibleArrayList<E> elements;
    private final Comparator<? super E> comparator;

    public ArraySet() {
        this(List.of(), null);
    }

    public ArraySet(final Collection<? extends E> collection) {
        this(collection, null);
    }

    public ArraySet(final Collection<? extends E> collection, final Comparator<? super E> comparator) {
        var tree = new TreeSet<E>(comparator);
        tree.addAll(collection);

        elements = new ReversibleArrayList<>(List.copyOf(tree));

        this.comparator = comparator;
    }

    public ArraySet(final ReversibleArrayList<E> list, final Comparator<? super E> comparator) {
        elements = list;

        this.comparator = comparator;
    }


    private final static class ReversibleArrayList<T> extends AbstractList<T> implements RandomAccess {

        private final List<T> list;
        private final boolean isReversed;

        private ReversibleArrayList(final List<T> list) {
            this(list, false);
        }

        private ReversibleArrayList(final List<T> list, final boolean isReversed) {
            this.list = list;
            this.isReversed = isReversed;
        }

        private ReversibleArrayList(final ReversibleArrayList<T> list, final boolean isReversed) {
            this.list = list.list;
            this.isReversed = list.isReversed ? !isReversed : isReversed;
        }

        @Override
        public T get(final int index) {
            return list == null ? null : list.get(isReversed ? size() - 1 - index : index);
        }

        @Override
        public int size() {
            return list == null ? 0 : list.size();
        }

        @Override
        public Iterator<T> iterator() {
            return super.iterator();
        }
    }

    private int find(final E e) {
        return Collections.binarySearch(elements, e, comparator);
    }

    private int indexOf(final E e) {
        int index = find(e);

        return index >= 0 ? index : -(index + 1);
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public E lower(final E e) {
        final int index = indexOf(e) - 1;

        return index < 0 || index >= size() ? null : elements.get(index);
    }

    @Override
    public E floor(final E e) {
        final int found = find(e);

        return found >= 0 && found < size() ? elements.get(found) : lower(e);
    }

    @Override
    public E ceiling(final E e) {
        final int found = indexOf(e);

        return found >= 0 && found < size() ? elements.get(found) : higher(e);
    }

    @Override
    public E higher(final E e) {
        int index = indexOf(e);

        if (index < 0 || index >= size()) {
            return null;
        }

        if (compare(elements.get(index), e) == 0) {
            if (++index < size()) {
                return elements.get(index);
            }

            return null;
        }

        return elements.get(index);
    }

    @Override
    public E pollFirst() {
        //        if (isEmpty()) {
        //            return null;
        //        }
        //
        //        var it = iterator();
        //        var res = it.next();
        //        it.remove();
        //
        //        return res;
        throw new UnsupportedOperationException("this set is immutable");
    }

    @Override
    public E pollLast() {
        //        if (isEmpty()) {
        //            return null;
        //        }
        //
        //        var it = descendingIterator();
        //        var res = it.next();
        //        it.remove();
        //
        //        return res;
        throw new UnsupportedOperationException("this set is immutable");
    }

    @Override
    public Iterator<E> iterator() {
        return elements.iterator();
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new ArraySet<>(new ReversibleArrayList<>(elements, true), Collections.reverseOrder(comparator));
    }

    @Override
    public Iterator<E> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public NavigableSet<E> subSet(final E fromElement, final boolean fromInclusive, final E toElement, final boolean toInclusive) {
        if (compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException("fromElement can not be greater than toElement");
        }

        if (isEmpty()) {
            return new ArraySet<>(List.of(), comparator);
        }

        return headSet(toElement, toInclusive).tailSet(fromElement, fromInclusive);
    }

    private int compare(final E e1, final E e2) {
        return comparator == null ? (Objects.equals(e1, e2) ? 0 : 1) : comparator.compare(e1, e2);
    }

    @Override
    public NavigableSet<E> headSet(final E toElement, final boolean inclusive) {
        if (isEmpty()) {
            return new ArraySet<>(List.of(), comparator);
        }

        final int found = indexOf(toElement);
        if (found == size()) {
            return new ArraySet<>(elements, comparator);
        }

        final boolean contains = inclusive && compare(elements.get(found), toElement) == 0;
        final int index = found + (!contains ? -1 : 0);


        return new ArraySet<>(new ReversibleArrayList<>(elements.subList(0, index + 1)), comparator);
    }

    static int count = 0;
    @Override
    public NavigableSet<E> tailSet(final E fromElement, final boolean inclusive) {
        if (isEmpty()) {
            return new ArraySet<>(List.of(), comparator);
        }

        final int found = indexOf(fromElement);
        if (found >= size()) {
            return new ArraySet<>(List.of(), comparator);
        }

        final boolean contains = !inclusive && compare(elements.get(found), fromElement) == 0;
        final int index = found + (contains ? +1 : 0);

        return new ArraySet<>(new ReversibleArrayList<>(elements.subList(index, elements.size())), comparator);
    }

    @Override
    public SortedSet<E> subSet(final E fromElement, final E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<E> headSet(final E toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<E> tailSet(final E fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public E first() {
        if (isEmpty()) {
            throw new NoSuchElementException("set is empty");
        }

        return elements.get(0);
    }

    @Override
    public E last() {
        if (isEmpty()) {
            throw new NoSuchElementException("set is empty");
        }

        return elements.get(elements.size() - 1);
    }

    @Override
    public boolean contains(final Object obj) {
        if (obj == null) {
            return false;
        }

        int index = find((E) obj);

        return index >= 0 && index < elements.size() &&
                (comparator == null
                        ? elements.get(index).equals(obj)
                        : compare(elements.get(index), (E) obj) == 0);
    }

    @Override
    public String toString() {
        final var res = new StringBuilder("[");

        for (final E e : this) {
            res.append(e.toString()).append(", ");
        }

        return (size() != 0 ? res.substring(0, res.length() - 2) : res) + "]";
    }
}
