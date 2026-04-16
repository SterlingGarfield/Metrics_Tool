package demo.folder;

public class PaymentGateway {

    public boolean charge(Customer customer, double amount) {
        if (customer.getLevel() > 2) {
            return amount < 2000;
        }
        return amount < 1000;
    }
}
