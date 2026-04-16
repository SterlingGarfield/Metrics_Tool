package demo;

public class PremiumAccount extends BaseAccount {
    private final AuditService auditService = new AuditService();

    public void withdraw(int amount) {
        if (amount < balance) {
            balance -= amount;
            auditService.record(amount);
        }
    }
}
