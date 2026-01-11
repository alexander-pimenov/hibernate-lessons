package sorokin.dev.utils;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Вспомогательный класс для работы с транзакциями.
 * Помогает убрать из сервисов повторяющихся кусков кода.
 * <p>
 * <strong>Использование транзакций в сервисах</strong><br>
 * Вынесли логику по работе с транзакциями во вспомогательные методы.
 * В этом классе у нас три метода: один для выполнения действия без результата, два - с результатом.
 * <p>
 * Также предусматривается вариант выполнения, когда один метод в транзакции вызывает другой
 * транзакционный метод:
 * <p>
 * <code>
 *     public <T> T executeInTransaction(Supplier<T> action)
 * </code>
 * <p>
 * В этом случае второй метод должен увидеть существующую транзакцию и не закрывать ее при окончании
 * работы. Эту транзакцию должен закрыть тот метод, который ее открыл.
 */
@Component
public class TransactionHelper {

    private final SessionFactory sessionFactory;

    public TransactionHelper(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Выполняет действие в транзакции, не возвращающее результат.
     *
     * @param action действие, передаваемое в качестве лямбды в вызывающем коде.
     *               <p>
     *               1. стартуем сессию в блоке try-with-resources, чтобы потом эта сессия закрывалась автоматически;
     *               2. начинаем транзакцию;
     *               3. передам сессию в action;
     *               4. коммитим транзакцию;
     *               5. в случае ошибки - откатываем транзакцию.
     */
    public void executeInTransaction(Consumer<Session> action) {
        Transaction transaction = null;
        try (Session session = sessionFactory.getCurrentSession()) {
//        try (Session session = sessionFactory.openSession()) {
            transaction = session.getTransaction();

            transaction.begin();

            // Выполняем действие в транзакции (наша ЛОГИКА)
            action.accept(session);

            transaction.commit();
            //session.getTransaction().commit();
        } catch (Exception e) {
            //Делаем проверку на null, т.к. транзакция может быть null, потому что
            //мы не открыли транзакцию, по какой-то причине.
            //Так мы не получим NullPointerException.
            if (transaction != null) {
                transaction.rollback();
            }
            //e.printStackTrace();
            //пробросим исключение на верх
            throw e;
        }
    }

    /**
     * Выполняет действие в транзакции и возвращает результат.
     *
     * @param action действие, передаваемое в качестве лямбды в вызывающем коде.
     * @param <T>    тип результата выполнения действия.
     * @return результат выполнения действия.
     */
    public <T> T executeInTransaction(Function<Session, T> action) {
        Transaction transaction = null;
        try (Session session = sessionFactory.getCurrentSession()) {
//        try (Session session = sessionFactory.openSession()) {
            transaction = session.getTransaction();
            transaction.begin();

            var result = action.apply(session);

            transaction.commit();
//            session.getTransaction().commit();
            return result;
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
    }


    /**
     * Использование транзакций в сервисах.
     * Логика по работе с транзакциями вынесена во вспомогательный метод.
     * Также предусматривается вариант выполнения, когда один метод в транзакции вызывает другой
     * транзакционный метод.
     * В этом случае второй метод должен увидеть существующую транзакцию и не закрывать ее при окончании
     * работы. Эту транзакцию должен закрыть тот метод, который ее открыл.
     * <p>
     *
     * @param action действие, передаваемое в качестве лямбды в вызывающем коде.
     * @param <T>    тип результата выполнения действия.
     *               В метод приходит Supplier<T> - действие, которое нужно выполнить в транзакции.
     *               Этот метод возвращает объект типа T.
     *               <p>
     * @return T - результат выполнения действия.
     */
    public <T> T executeInTransaction(Supplier<T> action) {
        // получаем текущую сессию
        var session = sessionFactory.getCurrentSession();
        // получаем транзакцию
        Transaction transaction = session.getTransaction();

        // Если транзакция уже не в статусе NOT_ACTIVE, т.е. она уже запущенна, то
        // просто продолжаем выполнение в этой транзакции
        if (!transaction.getStatus().equals(TransactionStatus.NOT_ACTIVE)) {
            return action.get();
        }
        // Иначе запускаем новую транзакцию, т.к. она еще не активна:
        try {
            session.beginTransaction();     // начинаем транзакцию
            T returnValue = action.get();
            transaction.commit();           // коммитим транзакцию
            return returnValue;
        } catch (Exception e) {
            //Делаем проверку на null, т.к. транзакция может быть null, потому что
            //мы не открыли транзакцию, по какой-то причине.
            //Так мы не получим NullPointerException.
            if (transaction != null) {
                transaction.rollback();     // откатываем транзакцию
            }
            //пробросим исключение на верх
            throw e;
        } finally {
            session.close();                // закрываем сессию
        }
    }
}
