package sorokin.dev.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс в котором хранится информация о курсах, на которых обучаются Студенты.
 */
@Entity //помечаем что это сущность для hibernate (JPA)
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String type;

    /**
     * ManyToMany - связь многие ко многим.
     * Эта связь реализуется при помощи вспомогательной таблицы.
     * Эту таблицу мы уже описали в классе Student и Hibernate возьмет её оттуда.
     * Здесь добавим только mappedBy = "courseList"
     * <p>
     */
    @ManyToMany(mappedBy = "courseList")
    private List<Student> studentList;

    public Course() {
    }

    public Course(
            String name,
            String type
    ) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
