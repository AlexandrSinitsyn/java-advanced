package info.kgeorgiy.ja.sinitsyn.student;

import info.kgeorgiy.java.advanced.student.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements StudentQuery, GroupQuery {

    private static final Comparator<Student> ID_COMPARATOR = Comparator.comparingInt(Student::getId);
    private static final Comparator<Group> GROUP_NAME_COMPARATOR = Comparator.comparing(Group::getName);
    private static final Comparator<Student> NAME_COMPARATOR =
            Comparator.comparing(Student::getLastName).reversed()
                    .thenComparing(Comparator.comparing(Student::getFirstName).reversed())
                    .thenComparing(ID_COMPARATOR);

    @Override
    public List<String> getFirstNames(final List<Student> students) {
        return mapCollection(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(final List<Student> students) {
        return mapCollection(students, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(final List<Student> students) {
        return mapCollection(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(final List<Student> students) {
        return mapCollection(students, s -> s.getFirstName() + " " + s.getLastName());
    }

    @Override
    public Set<String> getDistinctFirstNames(final List<Student> students) {
        return students.stream().map(Student::getFirstName)
                .sorted().collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public String getMaxStudentFirstName(final List<Student> students) {
        return students.stream()
                .max(ID_COMPARATOR)
                .map(Student::getFirstName)
                .orElse("");
    }

    @Override
    public List<Student> sortStudentsById(final Collection<Student> students) {
        return sortCollection(students.stream(), ID_COMPARATOR);
    }

    @Override
    public List<Student> sortStudentsByName(final Collection<Student> students) {
        return sortByName(students.stream());
    }

    @Override
    public List<Student> findStudentsByFirstName(final Collection<Student> students, final String name) {
        return filterCollection(students, Student::getFirstName, name);
    }

    @Override
    public List<Student> findStudentsByLastName(final Collection<Student> students, final String name) {
        return filterCollection(students, Student::getLastName, name);
    }

    @Override
    public List<Student> findStudentsByGroup(final Collection<Student> students, final GroupName group) {
        return filterCollection(students, Student::getGroup, group);
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(final Collection<Student> students, final GroupName group) {
        return findStudentsByGroup(students, group).stream()
                .collect(Collectors.toMap(Student::getLastName, Student::getFirstName, (existing, replacement) -> replacement));
    }

    @Override
    public List<Group> getGroupsByName(final Collection<Student> students) {
        return getGroupsBy(students, this::sortStudentsByName);
    }

    @Override
    public List<Group> getGroupsById(final Collection<Student> students) {
        return getGroupsBy(students, this::sortStudentsById);
    }

    @Override
    public GroupName getLargestGroup(final Collection<Student> students) {
        return findGroupBy(students, Collection::size, GROUP_NAME_COMPARATOR);
    }

    @Override
    public GroupName getLargestGroupFirstName(final Collection<Student> students) {
        return findGroupBy(students, list -> list.stream().map(Student::getFirstName).distinct().count(), GROUP_NAME_COMPARATOR.reversed());
    }

    private <T, R> List<R> mapCollection(final Collection<T> collection, final Function<T, R> function) {
        return collection.stream().map(function).toList();
    }

    private <T> List<T> sortCollection(final Stream<T> stream, final Comparator<T> comparator) {
        return stream.sorted(comparator).toList();
    }

    private List<Student> sortByName(final Stream<Student> stream) {
        return sortCollection(stream, NAME_COMPARATOR);
    }

    private <T> List<Student> filterCollection(final Collection<Student> collection, final Function<Student, T> function, final T test) {
        return sortByName(collection.stream().filter(t -> test.equals(function.apply(t))));
    }

    private List<Group> getGroupsBy(final Collection<Student> students, final Function<Collection<Student>, List<Student>> sortStudents) {
        return sortCollection(students.stream()
                        .collect(Collectors.groupingBy(Student::getGroup))
                        .entrySet().stream().map(entry -> new Group(entry.getKey(), sortStudents.apply(entry.getValue()))), GROUP_NAME_COMPARATOR);
    }

    private <T> GroupName findGroupBy(final Collection<Student> students,
                                  final ToLongFunction<Collection<Student>> wrapper,
                                  final Comparator<Group> comparatorForSame) {
        return getGroupsByName(students).stream()
                .map(g -> Map.entry(g, wrapper.applyAsLong(g.getStudents())))
                .max(Comparator.<Map.Entry<Group, Long>>comparingLong(Map.Entry::getValue)
                        .thenComparing(Map.Entry.comparingByKey(comparatorForSame)))
                .map(p -> p.getKey().getName())
                .orElse(null);
    }
}
