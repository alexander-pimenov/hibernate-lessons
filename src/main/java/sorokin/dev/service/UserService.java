package sorokin.dev.service;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.stereotype.Service;
import sorokin.dev.entity.User;
import sorokin.dev.utils.TransactionHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final AccountService accountService;
    private final SessionFactory sessionFactory;
    private final TransactionHelper transactionHelper;

    public UserService(AccountService accountService, SessionFactory sessionFactory, TransactionHelper transactionHelper) {
        this.accountService = accountService;
        this.sessionFactory = sessionFactory;
        this.transactionHelper = transactionHelper;
    }

    public User createUser(String login) {
        //Transaction transaction = null;
        //Сессию для создания пользователя мы оборачиваем в try-with-resources, чтобы она потом закрылась,
        //а для accountService.createAccount(user); - если зайти в него, не будем оборачивать в try-with-resources,
        //чтобы accountService.createAccount(user) не закрывал сессию.
//        try (Session session = sessionFactory.getCurrentSession()) {
//            transaction = session.getTransaction();
//            transaction.begin();
//
//            var existedUser = session.createQuery("FROM User WHERE login=:login", User.class)
//                    .setParameter("login", login)
//                    .getSingleResultOrNull();
//            if (existedUser != null) {
//                throw new IllegalArgumentException("User already exists with login=%s".formatted(login));
//            }
//            User user = new User(login, new ArrayList<>());
//
//            session.persist(user);
//            accountService.createAccount(user);
//
//            transaction.commit();
//            return user;
//        }
        //
        // Используем transactionHelper с корректной реализацией работы с транзакцией
        return transactionHelper.executeInTransaction(() -> {
            Session session = sessionFactory.getCurrentSession();
            var existedUser = session.createQuery("FROM User WHERE login=:login", User.class)
                    .setParameter("login", login)
                    .getSingleResultOrNull();
            if (existedUser != null) {
                throw new IllegalArgumentException("User already exists with login=%s".formatted(login));
            }
            User user = new User(login, new ArrayList<>());

            session.persist(user);
            accountService.createAccount(user);
            return user;
        });
    }

    /**
     * Метод для поиска пользователя по id
     *
     * @param id id пользователя
     * @return пользователь
     * <p>
     * Этот метод можно не оборачивать в транзакцию, т.к. он не изменяет данные в базе.
     */
    public Optional<User> findUserById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            User user = session.get(User.class, id);
            return Optional.of(user);
        }
    }

    /**
     * Метод для получения всех пользователей.
     * Его можно в транзакцию не оборачивать, т.к. он не изменяет данные в базе.
     *
     * @return список пользователей
     * <p>
     * "FROM User" - это HQL запрос, который выбирает всех пользователей из таблицы User, но у него есть проблема N+1.
     * Лучше сразу использовать запрос с join, чтобы не делать лишних запросов.
     */
    public List<User> getAllUsers() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("SELECT u FROM User u LEFT JOIN FETCH u.accountList", User.class)
                    .list();
        }
    }
}
