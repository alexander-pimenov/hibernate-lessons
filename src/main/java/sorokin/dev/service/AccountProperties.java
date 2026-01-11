package sorokin.dev.service;
//аннотация для чтения из ресурсов

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AccountProperties {

    @Value("${account.default-amount:500}")
    private final int defaultAccountAmount;
    @Value("${account.transfer-commission:10}")
    private final int transferCommission;

    public AccountProperties(int defaultAccountAmount, int transferCommission) {
        this.defaultAccountAmount = defaultAccountAmount;
        this.transferCommission = transferCommission;
    }

    public int getDefaultAccountAmount() {
        return defaultAccountAmount;
    }

    public int getTransferCommission() {
        return transferCommission;
    }

}
