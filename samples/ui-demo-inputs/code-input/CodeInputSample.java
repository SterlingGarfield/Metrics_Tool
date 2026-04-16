public class CodeInputSample {
    private int threshold = 10;

    public String classify(int value) {
        if (value < 0) {
            return "INVALID";
        }

        if (value > threshold) {
            return "HIGH";
        }

        return "NORMAL";
    }

    public int sumPositive(int[] values) {
        int total = 0;
        for (int value : values) {
            if (value > 0) {
                total += value;
            }
        }
        return total;
    }
}
