package demo;

public class BaseAccount {
    protected int balance;

    public void deposit(int amount) {
        balance += amount;
    }
}
