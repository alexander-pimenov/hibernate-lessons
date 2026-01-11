package sorokin.dev.entity;

import jakarta.persistence.*;
import sorokin.dev.service.GroupService;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс, в котором хранится информация о группах Студента.
 */
@Entity //помечаем что это сущность для hibernate (JPA)
@Table(name = "student_group")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "number")
    private String number;

    @Column(name = "grad_year")
    private Long graduationYear;

    /**
     * @OneToMany(mappedBy = "group") - указывает по какому полю в классе Student происходит мапинг на Group.
     * Также тут по дефолту стоит FetchType.LAZY, и список связанных сущностей не подгружается сразу из БД,
     * а только при обращении к нему.
     * FetchType.LAZY не всегда оптимален, если мы знаем, что нам нужны все подгружаемые сущности.
     * Поэтому в данном случае мы можем использовать FetchType.EAGER, и все подгрузится сразу.
     * Но при FetchType.EAGER мы можем наблюдать проблему N+1.
     * Это когда для получения всех связанных сущностей Hibernate делает N+1 запросов.
     * С FetchType.LAZY тоже не решает эту проблему, не смотря на то что при запросе не
     * подгружаются студенты сразу. Если нам понадобятся студенты, то мы всё равно будем делать запросы к БД.
     * <p>
     * Чтобы уйти от проблемы N+1, нужно использовать правильные запросы к БД. С помощью JOIN FETCH (смотри это в
     * сервисе {@link GroupService#findAll()}).
     */
    @OneToMany(mappedBy = "group", fetch = FetchType.EAGER)
    private List<Student> studentList = new ArrayList<>();

    public Group() {
    }

    public Group(
            String number,
            Long graduationYear
    ) {
        this.number = number;
        this.graduationYear = graduationYear;
    }

    public List<Student> getStudentList() {
        return studentList;
    }

    public void setStudentList(List<Student> studentList) {
        this.studentList = studentList;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Long getGraduationYear() {
        return graduationYear;
    }

    public void setGraduationYear(Long graduationYear) {
        this.graduationYear = graduationYear;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Group{" +
                "id=" + id +
                ", number='" + number + '\'' +
                ", graduationYear=" + graduationYear +
                '}';
    }
}
