public class SingleFileDemo {
    private int totalScore;

    public void addScore(int score) {
        if (score > 0) {
            totalScore += score;
        }
    }

    public String grade() {
        if (totalScore >= 90) {
            return "A";
        }
        if (totalScore >= 75) {
            return "B";
        }
        return "C";
    }

    public boolean needsReview() {
        return totalScore < 60;
    }
}
