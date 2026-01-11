package sorokin.dev.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Класс, в котором хранится информация о профиле Студента.
 */
@Entity //JPA сущность
@Table(name = "profiles") //Таблица в БД
public class Profile {

    /**
     * @GeneratedValue(strategy = GenerationType.IDENTITY) - стратегия генерации id, когда сохраняется сущность в БД.
     * GenerationType.IDENTITY - БД сама генерит id.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Описание о себе в профиле.
     */
    @Column(name = "bio")
    private String bio;

    /**
     * Последнее время, когда студент был у себя в профиле (в сети).
     */
    @Column(name = "last_seen_time")
    private LocalDateTime lastSeenTime;

    /**
     * Колонка в Профилях: student_id.
     * Связь с сущностью Студент. Поле, показывающее к какому студенту относится профиль.
     * Аннотация @OneToOne говорит, что сущность Студента должна подтягиваться из другой связной таблицы.
     * Т.е. "Студент" не хранится в качестве поля в таблице профиля, а хранится в отдельной таблице "Студент"
     * и подтягивается из нее.
     * Аннотация @JoinColumn(name = "student_id", referencedColumnName = "id") говорит,
     * как студент хранится в таблице профиля: под именем "student_id", а в таблице студента под именем "id".
     * В таблице Профилей есть идентификатор "student_id", который говорит к какому студенту относится профиль.
     * А в таблице студента есть "id", который равен "student_id" в таблице профилей.
     * name = "student_id" - показывает, как колонка в таблице профиля называется.
     * referencedColumnName = "id" - показывает, на какую колонку в таблице студента ссылается колонка "student_id".
     * <p>
     * Если посмотреть в БД на колонку "student_id", то там будет идентификатор из таблицы студентов из колонки "id" в
     * таблице студентов. Так же на ней будет навешен FK + unique (констреинт).
     * В колонку "student_id" можно записать только идентификатор, который есть в таблице студентов. Это из-за FK.
     * Но также можно поместить значение null, когда нет связи со студентом.
     * <p>
     * Благодаря наличию в профиле у колонки "student_id" FK, база данных не даст удалить студента из таблицы студентов,
     * если в профиле есть ссылка на него. Так же мы можем поместить null в эту колонку.
     */
    @OneToOne
    @JoinColumn(name = "student_id", referencedColumnName = "id")
    private Student student;

    public Profile() {
    }

    /**
     * Конструктор с тремя параметрами.
     * @param bio - описание о себе в профиле.
     * @param lastSeenTime - последнее время, когда студент был у себя в профиле (в сети).
     * @param student - студент.
     */
    public Profile(
            String bio,
            LocalDateTime lastSeenTime,
            Student student
    ) {
        this.bio = bio;
        this.lastSeenTime = lastSeenTime;
        this.student = student;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public LocalDateTime getLastSeenTime() {
        return lastSeenTime;
    }

    public void setLastSeenTime(LocalDateTime lastSeenTime) {
        this.lastSeenTime = lastSeenTime;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Profile{" +
                "id=" + id +
                ", bio='" + bio + '\'' +
                ", lastSeenTime=" + lastSeenTime +
                ", student=" + student +
                '}';
    }
}
