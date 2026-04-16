package demo.multi;

public class InventoryItem {
    private final String name;
    private int stock;

    public InventoryItem(String name, int stock) {
        this.name = name;
        this.stock = stock;
    }

    public String getName() {
        return name;
    }

    public int getStock() {
        return stock;
    }

    public void reduceStock(int amount) {
        stock -= amount;
    }
}
