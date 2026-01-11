package sorokin.dev.service;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;
import sorokin.dev.entity.LogEntry;
import sorokin.dev.entity.Student;
import sorokin.dev.utils.TransactionHelper;

import java.util.List;

@Service
public class LoggingService {
    private final SessionFactory sessionFactory;
    private final TransactionHelper transactionHelper;

    public LoggingService(SessionFactory sessionFactory, TransactionHelper transactionHelper) {
        this.sessionFactory = sessionFactory;
        this.transactionHelper = transactionHelper;
    }

    public void log(String message) {
        transactionHelper.executeInTransaction(session -> {
            session.persist(new LogEntry(message));
        });
    }

    public List<LogEntry> getAllLogs() {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery("SELECT s FROM Student s", LogEntry.class)
                    .list();
        }
//        return transactionHelper.executeInTransaction(session ->
//                session.createQuery("FROM LogEntry", LogEntry.class).list()
//        );
    }
}
