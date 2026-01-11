package sorokin.dev.service;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;
import sorokin.dev.entity.Profile;
import sorokin.dev.utils.TransactionHelper;

/**
 * Сервис для работы с профилями.
 */
@Service
public class ProfileService {

    private final SessionFactory sessionFactory;
    private final TransactionHelper transactionHelper;

    public ProfileService(SessionFactory sessionFactory,
                          TransactionHelper transactionHelper
    ) {
        this.sessionFactory = sessionFactory;
        this.transactionHelper = transactionHelper;
    }

    public Profile saveProfile(Profile profile) {
        return transactionHelper.executeInTransaction(session -> {
            session.persist(profile);
            return profile;
        });
    }

    /**
     * Возвращает профиль по id.
     * <p>
     * Поиск можно проводить без транзакции, т.к. он не изменяет данные, этот метод для чтения.
     * <p>
     *
     * @param id - id профиля
     * @return профиль
     */
    public Profile getProfileById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            if (id != null) {
                return session.get(Profile.class, id);
            }
        }
        return null;
    }
}
