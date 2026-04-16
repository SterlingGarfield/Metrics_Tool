package demo.folder;

public class PriorityOrder extends BaseOrder {
    private final PaymentGateway paymentGateway = new PaymentGateway();

    public String submit(Customer customer, double amount) {
        addAmount(amount);

        if (amount <= 0) {
            return "Amount error";
        }

        if (paymentGateway.charge(customer, amount)) {
            return "Paid for " + customer.getName();
        }

        return "Payment failed";
    }
}
