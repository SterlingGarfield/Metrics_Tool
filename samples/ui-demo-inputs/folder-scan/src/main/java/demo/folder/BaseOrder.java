package demo.folder;

public class BaseOrder {
    protected double totalAmount;

    public void addAmount(double amount) {
        totalAmount += amount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }
}
