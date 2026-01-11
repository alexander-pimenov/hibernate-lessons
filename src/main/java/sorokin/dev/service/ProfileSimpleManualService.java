package sorokin.dev.service;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;
import sorokin.dev.entity.Profile;
import sorokin.dev.entity.Student;

/**
 * Сервис для работы с профилями.
 */
@Service
public class ProfileSimpleManualService {

    private final SessionFactory sessionFactory;

    public ProfileSimpleManualService(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Сохранение профиля.
     *
     * @param profile профиль
     * @return сохраненный профиль
     */
    public Profile saveProfile(Profile profile) {
        Session session = sessionFactory.openSession();
        if (profile != null) {
            session.beginTransaction();
            session.persist(profile);
            session.getTransaction().commit();
        }
        session.close();
        return profile;
    }

    /**
     * Возвращает профиль по id.
     * <p>
     * Поиск можно проводить без транзакции.
     *
     * @param id - id профиля
     * @return профиль
     */
    public Profile getProfileById(Long id) {
        Session session = sessionFactory.openSession();
        Profile profileById = null;
        if (id != null) {
            session.beginTransaction();
            profileById = session.get(Profile.class, id);
            session.getTransaction().commit();
        }
        session.close();
        return profileById;
    }

    /**
     * Удаляет профиль.
     *
     * @param id - id профиля
     */
    public void deleteProfileById(Long id) {
        Session session = sessionFactory.openSession();
        if (id != null) {
            session.beginTransaction();
            Profile profileById = session.get(Profile.class, id);
            if (profileById != null) {
                session.remove(profileById);   // Удаляем объект из БД.
            }
            session.getTransaction().commit();
        }
        session.close();
    }
}
