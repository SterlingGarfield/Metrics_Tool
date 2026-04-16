package demo.multi;

public class StockAlert {

    public String buildMessage(InventoryItem item) {
        if (item.getStock() < 5) {
            return item.getName() + " is low on stock";
        }
        return item.getName() + " is stable";
    }
}
