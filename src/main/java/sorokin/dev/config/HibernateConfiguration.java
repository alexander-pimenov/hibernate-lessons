package sorokin.dev.config;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sorokin.dev.entity.*;
import sorokin.dev.service.StudentService;

/**
 * Конфигурация для подключения к Hibernate
 * <p>
 * <strong>Привязка единой сессии к потоку исполнения</strong><br>
 * В многопоточных приложениях может возникнуть проблема с управлением сессиями Hibernate. Если
 * сессия используется несколькими потоками одновременно, это может привести к неожиданным ошибкам
 * и некорректной работе приложения. Необходимо обеспечить, чтобы каждая сессия была связана только с
 * одним потоком исполнения.
 * <strong>Решение</strong><br>
 * Hibernate предоставляет механизм для управления сессиями в одном потоке с использованием параметра
 * `hibernate.current_session_context_class`. Установка значения этого параметра в `thread` позволяет Hibernate
 * автоматически привязывать сессию к текущему потоку, что обеспечивает безопасное использование
 * сессий в многопоточной среде.
 * Для этой настройки нужно создать класс конфигурации Hibernate и установить свойство
 * `hibernate.current_session_context_class` в `thread`.<br>
 * <code>
 * configuration.setProperty("hibernate.current_session_context_class", "thread");
 * </code>
 * Также нужно будет использовать `getCurrentSession()` для получения текущей сессии в каждом потоке. Пример
 * использования в методе #executeInTransaction в сервисе хелпере {@link sorokin.dev.utils.TransactionHelper}.
 * <p>
 * Теперь каждая сессия будет связана с текущим потоком, что обеспечит безопасное использование сессий
 * в многопоточной среде.
 */
@Configuration
public class HibernateConfiguration {

    /**
     * Метод для создания SessionFactory.
     * Это есть бин, который будет создан при запуске приложения и помещен в контекст Spring.
     * <p>
     * В Hibernate основной класс для работы с БД - это Session. Он позволяет выполнять запросы к БД.
     * <p>
     * Этот метод создает SessionFactory, которая будет создавать нам Hibernate Session, которые будут использоваться
     * для выполнения запросов к БД.
     *
     * @return SessionFactory
     */
    @Bean
    public SessionFactory sessionFactory() {
        // Создаем объект org.hibernate.cfg.Configuration, для настройки Hibernate, который будет содержать информацию о наших классах и БД
        org.hibernate.cfg.Configuration configuration = new org.hibernate.cfg.Configuration();

        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties())
                .build();

        // Добавляем свойства, по которым будет работать Hibernate,
        // а также аннотированные классы и указываем настройки подключения к БД.
        // Чтобы Hibernate знал, что Student.class, Profile.class, Group.class, Course.class - это наши классы/сущности,
        // которые мы хотим сохранять в БД (мы должны их тут указать).
        configuration
                .addAnnotatedClass(Student.class)
                .addAnnotatedClass(Profile.class)
                .addAnnotatedClass(Group.class)
                .addAnnotatedClass(Course.class)
                .addPackage("sorokin.dev") // добавляем пакет, чтобы Hibernate мог найти наши классы
                .setProperty("hibernate.connection.driver_class", "org.postgresql.Driver")
                .setProperty("hibernate.connection.url", "jdbc:postgresql://localhost:6432/nsix") //было - "jdbc:postgresql://localhost:5432/postgres"
                .setProperty("hibernate.connection.username", "postgres")
                .setProperty("hibernate.connection.password", "postgres") //было - root
                .setProperty("hibernate.show_sql", "true")
                .setProperty("hibernate.hbm2ddl.auto", "create-drop")
                .setProperty("hibernate.current_session_context_class", "thread");
        //create-drop - при запуске приложения будет созданы таблицы, а при закрытии - удалены
        //Будет выполнено это:
        //Hibernate: drop table if exists courses cascade
        //Hibernate: drop table if exists profiles cascade
        //
        //update - при запуске приложения будут обновляться таблицы, если они уже существуют и у них добавляются новые поля

        return configuration.buildSessionFactory();
    }

    @Bean("sessionFactorySecond")
    public SessionFactory sessionFactorySecond() {
        // Создаем объект org.hibernate.cfg.Configuration, для настройки Hibernate, который будет содержать информацию о наших классах и БД
        org.hibernate.cfg.Configuration configuration = new org.hibernate.cfg.Configuration();

        // Добавляем свойства, по которым будет работать Hibernate,
        // а также аннотированные классы (сущности JPA) и указываем настройки подключения к БД.
        // Чтобы Hibernate знал, что Student.class, Profile.class, Group.class, Course.class - это наши классы/сущности,
        // которые мы хотим сохранять в БД (мы должны их тут указать).
        configuration
                .addAnnotatedClass(Student.class)
                .addAnnotatedClass(Profile.class)
                .addAnnotatedClass(Group.class)
                .addAnnotatedClass(Course.class)
                .addAnnotatedClass(User.class)
                .addAnnotatedClass(Account.class)
                .addPackage("sorokin.dev") // добавляем пакет, чтобы Hibernate мог найти наши классы
                .setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
                .setProperty("hibernate.connection.driver_class", "org.postgresql.Driver")
                .setProperty("hibernate.connection.url", "jdbc:postgresql://localhost:6432/nsix") //было - "jdbc:postgresql://localhost:5432/postgres"
                .setProperty("hibernate.connection.username", "postgres")
                .setProperty("hibernate.connection.password", "postgres") //было - root
                .setProperty("hibernate.show_sql", "true")
                .setProperty("hibernate.hbm2ddl.auto", "create-drop")
                .setProperty("hibernate.current_session_context_class", "thread");
        //create-drop - при запуске приложения будет созданы таблицы, а при закрытии - удалены
        //Будет выполнено это:
        //Hibernate: drop table if exists courses cascade
        //Hibernate: drop table if exists profiles cascade
        //
        //update - при запуске приложения будут обновляться таблицы, если они уже существуют и у них добавляются новые поля

        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties())
                .build();

        return configuration.buildSessionFactory(serviceRegistry);
    }

}
