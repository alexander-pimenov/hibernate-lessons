package sorokin.dev.service;

import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;
import sorokin.dev.entity.Course;
import sorokin.dev.utils.TransactionHelper;
import sorokin.dev.entity.Student;

import java.util.List;

@Service
public class CourseService {

    private final SessionFactory sessionFactory;
    private final TransactionHelper transactionHelper;

    public CourseService(
            SessionFactory sessionFactory,
            TransactionHelper transactionHelper
    ) {
        this.sessionFactory = sessionFactory;
        this.transactionHelper = transactionHelper;
    }

    /**
     * Метод сохранения в БД объекта Course
     * @param course объект Course
     * @return объект Course с заполненным полем id
     */
    public Course saveCourse(Course course) {
        return transactionHelper.executeInTransaction(session -> {
            session.persist(course);
            return course;
        });
    }

    /**
     * Метод добавления/зачисления студента на курс (в список студентов курса).
     * @param courseId id курса
     * @param studentId id студента
     */
    public void enrollStudentToCourse(
            Long courseId,
            Long studentId
    ) {
        transactionHelper.executeInTransaction(session -> {
            //Этот код закомментирован,т.к. он не оптимален. Есть лишние запросы к БД.
//            var student = session.get(Student.class, studentId);
//            var course = session.get(Course.class, courseId);
//            student.getCourseList().add(course);

            //Тут лучше использовать nativeQuery с SQL, чтобы Hibernate не делал лишних запросов к БД.
            //Но тут мы должны быть точно уверенны, что такой курс и такой студент существуют в БД !!!
            String sql = """
                    INSERT INTO student_courses (student_id, course_id)
                    VALUES (:studentId, :courseId);
                    """;

            session.createNativeQuery(sql, Void.class)
                    .setParameter("studentId", studentId)
                    .setParameter("courseId", courseId)
                    .executeUpdate();
        });
    }

    public List<Student> getStudentsOnCourse(Long id) {
        return null;
    }
}
