package demo.multi;

public class InventoryService {
    private final StockAlert stockAlert = new StockAlert();

    public String sell(InventoryItem item, int amount) {
        if (amount <= 0) {
            return "Invalid amount";
        }

        if (item.getStock() < amount) {
            return "Not enough stock";
        }

        item.reduceStock(amount);
        return stockAlert.buildMessage(item);
    }
}
