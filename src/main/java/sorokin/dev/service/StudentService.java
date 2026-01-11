package sorokin.dev.service;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;
import sorokin.dev.entity.Student;
import sorokin.dev.utils.TransactionHelper;

import java.util.List;

/**
 * Сервис для работы со студентами.
 */
@Service
public class StudentService {

    private final SessionFactory sessionFactory;
    private final TransactionHelper transactionHelper;

    public StudentService(SessionFactory sessionFactory,
                          TransactionHelper transactionHelper
    ) {
        this.sessionFactory = sessionFactory;
        this.transactionHelper = transactionHelper;
    }

    /**
     * Сохраняет студента.
     * @param student студент
     * @return сохраненный студент
     */
    public Student saveStudent(Student student) {
        //вернем студента, которого передали в качестве параметра в лямбду.
        return transactionHelper.executeInTransaction(session -> {
            session.persist(student);
            return student;
        });
    }

    /**
     * Удаляет студента.
     * @param id - id студента
     */
    public void deleteStudent(Long id) {
        transactionHelper.executeInTransaction(session -> {
            Student studentForDelete = session.get(Student.class, id);
            session.remove(studentForDelete);
        });
    }

    /**
     * Возвращает студента по id.
     * @param id - id студента
     * @return студент
     * Этот метод может работать без транзакции, т.к. он не изменяет данные, этот метод для чтения.
     */
    public Student getStudentById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(Student.class, id);
        }
    }

    /**
     * Возвращает всех студентов.
     * @return список студентов.
     * Этот метод может работать без транзакции, т.к. он не изменяет данные, этот метод для чтения.
     */
    public List<Student> findAllStudents() {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery("SELECT s FROM Student s", Student.class)
                    .list();
        }
    }

    /**
     * Обновляет студента.
     * @param student студент
     * @return обновленный студент
     */
    public Student updateStudent(Student student) {
        return transactionHelper.executeInTransaction(session -> {
            return session.merge(student);
        });
    }
}
