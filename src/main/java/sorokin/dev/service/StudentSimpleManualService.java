package sorokin.dev.service;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.stereotype.Service;
import sorokin.dev.entity.Student;

import java.util.List;

/**
 * Сервис для работы со студентами.
 * Для работы с БД. Используется только SessionFactory/Session.
 * <p>
 * Более корректно использовать для открытия сессии try-with-resources.
 * Этот блок автоматически будет закрывать сессию. И не будет утечек памяти.
 * Также нужно предусмотреть ролл-бэк транзакции.
 */
@Service
public class StudentSimpleManualService {

    private final SessionFactory sessionFactory;

    public StudentSimpleManualService(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Сохраняет студента.
     *
     * @param student студент
     * @return сохраненный студент
     */
    public Student saveStudent(Student student) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            if (student != null) {
                transaction = session.getTransaction();
                transaction.begin();

                session.persist(student);

                session.getTransaction().commit();
            }
            return student;
        } catch (Exception e) {
            //Делаем проверку на null, т.к. транзакция может быть null, потому что
            //мы не открыли транзакцию, по какой-то причине.
            //Так мы не получим NullPointerException.
            if (transaction != null) {
                transaction.rollback();
            }
            //пробросим исключение на верх
            throw e;
        }
        //после выхода из блока try сессия будет закрыта - session.close();

        //Старый рабочий, но не совсем корректный.
        /*Session session = sessionFactory.openSession();
        if (student != null) {
            session.beginTransaction();
            session.persist(student);
            session.getTransaction().commit();
        }
        session.close();
        return student;
         */
    }

    /**
     * Удаляет студента.
     *
     * @param id - id студента
     */
    public void deleteStudent(Long id) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            if (id != null) {
                transaction = session.getTransaction();
                transaction.begin();
                Student studentById = session.get(Student.class, id);
                if (studentById != null) {
                    session.remove(studentById);   // Удаляем объект из БД.
                }
                session.getTransaction().commit();
            }
        } catch (Exception e) {
            //Делаем проверку на null, т.к. транзакция может быть null, потому что
            //мы не открыли транзакцию, по какой-то причине.
            //Так мы не получим NullPointerException.
            if (transaction != null) {
                transaction.rollback();
            }
            //пробросим исключение на верх
            throw e;
        }

        //Старый рабочий, но не совсем корректный.
        /*
        Session session = sessionFactory.openSession();
        if (id != null) {
            session.beginTransaction();
            Student studentById = session.get(Student.class, id);
            if (studentById != null) {
                session.remove(studentById);   // Удаляем объект из БД.
            }
            session.getTransaction().commit();
        }
        session.close();
        */
    }

    /**
     * Возвращает студента по id.
     *
     * @param id - id студента
     * @return студент
     */
    public Student getStudentById(Long id) {
        Session session = sessionFactory.openSession();
        Student studentById = null;
        if (id != null) {
            session.beginTransaction();
            studentById = session.get(Student.class, id);
            session.getTransaction().commit();
        }
        session.close();
        return studentById;

//        try (Session session = sessionFactory.openSession()) {
//            return session.get(Student.class, id);
//        }
    }

    /**
     * Возвращает всех студентов.
     *
     * @return список студентов.
     */
    public List<Student> findAll() {
        Session session = sessionFactory.openSession();
        List<Student> students = session.createQuery("SELECT s FROM Student s", Student.class)
                .list();
        session.close();
        return students;
//        try (Session session = sessionFactory.openSession()) {
//            return session
//                    .createQuery("SELECT s FROM Student s", Student.class)
//                    .list();
//        }
    }

    /**
     * Обновляет студента.
     *
     * @param student студент
     * @return обновленный студент
     */
    public Student updateStudent(Student student) {
        Session session = sessionFactory.openSession();
        if (student != null) {
            session.beginTransaction();
            student = session.merge(student);
            session.getTransaction().commit();
        }
        session.close();
        return student;
    }
}
