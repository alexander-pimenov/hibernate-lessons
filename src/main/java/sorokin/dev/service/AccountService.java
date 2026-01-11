package sorokin.dev.service;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;
import sorokin.dev.entity.Account;
import sorokin.dev.entity.User;
import sorokin.dev.utils.TransactionHelper;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class AccountService {

    private final SessionFactory sessionFactory;
    private final AccountProperties accountProperties;
    private final TransactionHelper transactionHelper;

    public AccountService(
            SessionFactory sessionFactory, AccountProperties accountProperties, TransactionHelper transactionHelper) {
        this.sessionFactory = sessionFactory;
        this.accountProperties = accountProperties;
        this.transactionHelper = transactionHelper;
    }

    /**
     * Создает новый аккаунт для пользователя.<br/>
     * Использует транзакцию.
     *
     * @param user Пользователь
     * @return Созданный аккаунт
     */
    public Account createAccount(User user) {
        return transactionHelper.executeInTransaction(() -> {
                    Account newAccount = new Account(
                            user,
                            accountProperties.getDefaultAccountAmount()
                    );
                    //Возьмем сессию, которая привязана к потоку:
                    var session = sessionFactory.getCurrentSession();
                    session.persist(newAccount);
                    return newAccount;
                }
        );
    }

    /**
     * Ищет аккаунт по id.
     * Его можно в транзакцию не оборачивать, т.к. он не изменяет данные в базе.
     *
     * @param id id аккаунта
     * @return Optional с аккаунтом, если он найден
     */
    public Optional<Account> findAccountById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            Account account = session.get(Account.class, id);
            return Optional.of(account);
        }
    }

    private Optional<Account> findAccountByIdInCurrentSession(Long id) {
        Session session = sessionFactory.getCurrentSession();
        Account account = session.get(Account.class, id);
        return Optional.of(account);
    }


    /**
     * Метод для пополнения счета.<br/>
     *
     * @param accountId      id счета (Аккаунт)
     * @param moneyToDeposit количество денег для пополнения
     */
    public void depositAccount(Long accountId, int moneyToDeposit) {
        var account = findAccountById(accountId).orElseThrow(
                () -> new IllegalArgumentException("No such account: id=%s".formatted(accountId))
        );

        if (moneyToDeposit <= 0) {
            throw new IllegalArgumentException(
                    ("Cannot deposit not positive amount: amount=%s")
                            .formatted(moneyToDeposit)
            );
        }
        transactionHelper.executeInTransaction(() -> {
            //Сделаем так, чтобы текущая сессия начала следить за этим аккаунтом.
            //Нам вернется объект, который уже находится в базе и за которым будет следить сессия.
            var acc = sessionFactory.getCurrentSession().merge(account);
            //Пополняем счет:
            acc.setMoneyAmount(acc.getMoneyAmount() + moneyToDeposit);
            return 0; //Не используется, просто что-то вернули.
        });
    }

    /**
     * Метод для пополнения счета.<br/>
     *
     * @param accountId      id счета (Аккаунт)
     * @param moneyToDeposit количество денег для пополнения
     */
    public void depositAccountAdvanced(Long accountId, int moneyToDeposit) {
        transactionHelper.executeInTransaction(() -> {
            var account = findAccountById(accountId).orElseThrow(
                    () -> new IllegalArgumentException("No such account: id=%s".formatted(accountId))
            );

            if (moneyToDeposit <= 0) {
                throw new IllegalArgumentException(
                        ("Cannot deposit not positive amount: amount=%s")
                                .formatted(moneyToDeposit)
                );
            }
            //Пополняем счет:
            account.setMoneyAmount(account.getMoneyAmount() + moneyToDeposit);
            return 0; //Не используется, просто что-то вернули.
        });
    }

    /**
     * Метод для снятия денег со счета.
     *
     * @param accountId        id счета (Аккаунт)
     * @param amountToWithdraw количество денег для снятия.
     */
    public void withdrawFromAccount(Long accountId, int amountToWithdraw) {
        var account = findAccountById(accountId).orElseThrow(
                () -> new IllegalArgumentException("No such account: id=%s".formatted(accountId))
        );
        if (amountToWithdraw <= 0) {
            throw new IllegalArgumentException(
                    ("Cannot withdraw not positive amount: amount=%s")
                            .formatted(amountToWithdraw)
            );
        }
        if (account.getMoneyAmount() < amountToWithdraw) {
            throw new IllegalArgumentException(
                    ("Cannot withdraw from account: id=%s, moneyAmount=%s, " +
                            "attemptedTransfer=%s")
                            .formatted(accountId, account.getMoneyAmount(), amountToWithdraw)
            );
        }
        transactionHelper.executeInTransaction(() -> {
            //Сделаем так, чтобы текущая сессия начала следить за этим аккаунтом.
            //Нам вернется объект, который уже находится в базе и за которым будет следить сессия.
            var acc = sessionFactory.getCurrentSession().merge(account);
            //Пополняем счет:
            acc.setMoneyAmount(acc.getMoneyAmount() - amountToWithdraw);
            return 0; //Не используется, просто что-то вернули.
        });
    }

    /**
     * Метод для снятия денег со счета.
     *
     * @param accountId        id счета (Аккаунт)
     * @param amountToWithdraw количество денег для снятия.
     */
    public void withdrawFromAccountAdvanced(Long accountId, int amountToWithdraw) {
        transactionHelper.executeInTransaction(() -> {
            var account = findAccountByIdInCurrentSession(accountId).orElseThrow(
                    () -> new IllegalArgumentException("No such account: id=%s".formatted(accountId))
            );
            if (amountToWithdraw <= 0) {
                throw new IllegalArgumentException(
                        ("Cannot withdraw not positive amount: amount=%s")
                                .formatted(amountToWithdraw)
                );
            }
            if (account.getMoneyAmount() < amountToWithdraw) {
                throw new IllegalArgumentException(
                        ("Cannot withdraw from account: id=%s, moneyAmount=%s, " +
                                "attemptedTransfer=%s")
                                .formatted(accountId, account.getMoneyAmount(), amountToWithdraw)
                );
            }
            //Пополняем счет:
            account.setMoneyAmount(account.getMoneyAmount() - amountToWithdraw);
            return 0; //Не используется, просто что-то вернули.
        });
    }

    /**
     * Метод для закрытия счета (Аккаунта).
     *
     * @param accountId id счета (Аккаунт)
     * @return Удаленный/закрытый аккаунт.
     */
    public Account closeAccount(Long accountId) {
        return transactionHelper.executeInTransaction(() -> {

            var accountToRemove = findAccountByIdInCurrentSession(accountId).orElseThrow(
                    () -> new IllegalArgumentException("No such account: id=%s".formatted(accountId))
            );

            //Проверим, что у пользователя остался хотя бы один счет
            List<Account> accountList = accountToRemove.getUser().getAccountList();
            if (accountList.size() == 1) {
                throw new IllegalArgumentException("Can't close the only one account");
            }

            //Ищем аккаунт, на который будем переводить деньги, с аккаунта, который мы удаляем:
            Account accountToDeposit = accountList.stream()
                    .filter(it -> !Objects.equals(it.getId(), accountId))
                    .findFirst()
                    .orElseThrow();

            //Зачисляем деньги с удаляемого аккаунта на этот найденный аккаунт:
            accountToDeposit.setMoneyAmount(accountToDeposit.getMoneyAmount() + accountToRemove.getMoneyAmount());

            //Удаляем аккаунт в текущей сессии:
            sessionFactory.getCurrentSession().remove(accountToRemove);
            return accountToRemove;
        });
    }

    /**
     * Метод для перевода денег с одного счета на другой.<br/>
     * @param fromAccountId id счета (Аккаунт) с которого переводим
     * @param toAccountId   id счета (Аккаунт) на который переводим
     * @param amountToTransfer количество денег для перевода
     */
    public void transfer(Long fromAccountId, Long toAccountId, int amountToTransfer) {
        if (amountToTransfer <= 0) {
            throw new IllegalArgumentException("Cannot transfer. Amount to transfer must be positive: amount=%s"
                    .formatted(amountToTransfer));
        }
        transactionHelper.executeInTransaction(() -> {

            var accountFrom = findAccountByIdInCurrentSession(fromAccountId).orElseThrow(
                    () -> new IllegalArgumentException("No such account: id=%s".formatted(fromAccountId))
            );

            var accountTo = findAccountByIdInCurrentSession(toAccountId).orElseThrow(
                    () -> new IllegalArgumentException("No such account: id=%s".formatted(toAccountId))
            );
            if (accountFrom.getMoneyAmount() < amountToTransfer) {
                throw new IllegalArgumentException(
                        ("Cannot transfer from account: id=%s, moneyAmount=%s," +
                                "attemptedTransfer=%s")
                                .formatted(accountFrom, accountFrom.getMoneyAmount(), amountToTransfer)
                );
            }
            int totalAmountToDeposit = !accountTo.getUser().getId().equals(accountFrom.getUser().getId())
                    ? (int) (amountToTransfer * (1 - accountProperties.getTransferCommission()))
                    : amountToTransfer;
            accountFrom.setMoneyAmount(accountFrom.getMoneyAmount() - amountToTransfer);
            accountTo.setMoneyAmount(accountTo.getMoneyAmount() + totalAmountToDeposit);
            return 0; //Не используется, просто что-то вернули.
        });
    }
}
