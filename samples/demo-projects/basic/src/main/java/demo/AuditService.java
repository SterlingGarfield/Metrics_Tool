package demo;

public class AuditService {
    public void record(int amount) {
        System.out.println("audit:" + amount);
    }
}
