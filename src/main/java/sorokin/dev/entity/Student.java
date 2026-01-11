package sorokin.dev.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity //помечаем что это сущность для hibernate (JPA)
@Table(name = "students") //указываем имя таблицы
public class Student {

    /**
     * @GeneratedValue(strategy = GenerationType.IDENTITY) - стратегия генерации id, когда сохраняется сущность в БД.
     * GenerationType.IDENTITY - БД сама генерит id.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Аннотация @Column name = "name" - указывает имя колонки в таблице.
     * Если её не писать, то имя колонки будет такое же, как у поля.
     * unique = true - поле уникальное, будет создан констрейнт уникальности, который не допустит дублирования.
     * nullable = false - поле не может быть null.
     * <p>
     * Использование аннотации @Column - это хорошая практика, т.к. позволяет не зависеть от имени поля класса, которое
     * может меняться, например, во время рефакторинга.
     */
    @Column(name = "name", unique = false, nullable = false)
    private String name;

    @Column(name = "student_age")
    private Integer age;

    /**
     * @OneToOne - аннотация, которая указывает, что у сущности Student есть одна сущность Profile.
     * Аннотацию @JoinColumn тут уже не пишем, т.к. она уже прописана в Profile.
     * mappedBy = "student" - указывает по какому полю в классе Profile идет связь с классом Student (
     * на какое поле мапить поле profile в классе Student в классе Profile).
     * <p>
     * Благодаря наличию в Профиле у колонки "student_id" FK, база данных не даст удалить студента из таблицы студентов,
     * если в профиле есть ссылка на него. Но мы можем поместить null в "student_id" колонке в профиле.
     * Также не сможем удалить профиль, если у него есть ссылка на студента.
     * <p>
     * Чтобы решить проблему при удалении профиля или студента, можно сначала удалить профиль.
     * <p>
     * Также можно это решить с помощью CascadeType.REMOVE:
     * cascade = CascadeType.REMOVE
     * <p>
     * На реальных проектах использование таких каскадных операций не рекомендуется. потому что это
     * может приводить к неожиданным поведениям системы.
     * <p>
     * BEST PRACTICE:
     * Мы сами управляем сущностями и их связями. Например, мы сами удаляем сначала все связанные сущности,
     * а потом основную сущность. Это нужно для того, чтобы нас самим было понятно, что происходит.
     * Чтобы не было неочевидных последствий от использования каскадных операций.
     */
    @OneToOne(mappedBy = "student", cascade = CascadeType.REMOVE)
    private Profile profile;


    /**
     * @ManyToOne - аннотация, которая указывает, что у сущности Student есть одна сущность Group.
     * Много студентов относится к одной группе.
     * Так мы привязываем студента к Группе.
     * @JoinColumn(name = "group_id") - указывает на колонку, которая будет FK в таблице студентов на таблицу с группами,
     * т.е. указывает по какой колонке нужно производить join. Имя колонки - group_id.
     * Т.е. у Студента будет колонка group_id, которая будет FK на таблицу с группами.
     * Когда Hibernate будет генерировать таблицу студентов, то в ней будет колонка group_id.
     * Т.к. появилась связь сущностей через FK, то Hibernate не сможет удалить одну из них пока есть ссылка на другую.
     * Чтобы удалить Группу, то нужно сперва удалить всех Студентов, которые относятся к ней, а потом только сможем
     * удалить и саму Группу.
     */
    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    /**
     * ManyToMany - связь многие ко многим.
     * Эта связь реализуется при помощи вспомогательной таблицы.
     * с помощью @JoinTable мы описываем эту вспомогательную/соединяющую таблицу.
     * <p>
     * У нас будет таблица "student_courses". У неё будут колонки "student_id" и "course_id".
     * <p>
     * joinColumns - это для колонки из нашей сущности, здесь это Student, а inverseJoinColumns - для колонки из другой сущности - Course.
     * <p>
     * "student_id" - FK соответствует "id" студента из таблицы "students", а "course_id" - FK соответствует "id" курса из таблицы "courses".
     * <p>
     * По дефолту список курсов у студента будет пустой - List<Course> courseList = new ArrayList<>();
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "student_courses", //имя соединяющей таблицы, которая будет создана в БД
            joinColumns = @JoinColumn(name = "student_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "course_id", referencedColumnName = "id")
    )
    private List<Course> courseList = new ArrayList<>();

    /**
     * Конструктор по умолчанию.
     * Все сущности, которые являются JPA сущностями, должны иметь конструктор по умолчанию без параметров.
     */
    public Student() {
    }

    public Student(
            String name,
            Integer age,
            Group group
    ) {
        this.name = name;
        this.age = age;
        this.group = group;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public List<Course> getCourseList() {
        return courseList;
    }

    public void setCourseList(List<Course> courseList) {
        this.courseList = courseList;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
