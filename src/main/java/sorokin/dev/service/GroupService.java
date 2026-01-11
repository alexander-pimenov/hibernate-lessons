package sorokin.dev.service;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;
import sorokin.dev.entity.Group;
import sorokin.dev.utils.TransactionHelper;

import java.util.List;

/**
 * Сервис по работе с группами.
 *
 */
@Service
public class GroupService {

    private final SessionFactory sessionFactory;
    private final TransactionHelper transactionHelper;

    public GroupService(
            SessionFactory sessionFactory,
            TransactionHelper transactionHelper
    ) {
        this.sessionFactory = sessionFactory;
        this.transactionHelper = transactionHelper;
    }

    public Group saveGroup(
            String number,
            Long gradYear
    ) {
        return transactionHelper.executeInTransaction(session -> { // Возвращаем Group из метода
            var group = new Group(number, gradYear);
            session.persist(group);
            return group; // Возвращаем сохраненную группу из лямбды
        });
    }

    /**
     * Метод возвращает список всех групп.
     * @return List<Group>
     * В этом методе используется корректный запрос, который решает проблему N+1.
     * Одним запросом мы получаем данные о группах, а также о студентах из этих групп и профилях студентов.
     */
    public List<Group> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("""
                    SELECT g from Group g
                    left join fetch g.studentList s
                    left join fetch s.profile
                    """, Group.class)
                    .list();
        }
    }

    /**
     * Метод возвращает список всех групп.
     * @return List<Group>
     * В этом методе используется НЕ корректный запрос, в котором наблюдаем проблему N+1,
     * когда включен FetchType.EAGER. {@link Group#studentList}
     */
    public List<Group> findAllWithNPlusOneProblem() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("""
                    SELECT g from Group g
                    """, Group.class)
                    .list();
        }
    }

    /**
     * Возвращает Группу по id.
     * @param id - id группы
     * @return Group
     * Этот метод может работать без транзакции, т.к. он не изменяет данные, этот метод для чтения.
     */
    public Group getGroupById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(Group.class, id);
        }
    }


}
