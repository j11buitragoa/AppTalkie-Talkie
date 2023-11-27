package uni.tesis.interfazfinal;

import android.app.Application;

public class Points extends Application {
    private int totalPoints = 0;
    private static final int NUM_LEVELS = 4;

    private int[] pointsPerLevel = new int[NUM_LEVELS];
    private MainActivity mainActivity;
    public int getTotalPoints() {
        return totalPoints;
    }

    public int getPointsForLevel(int level) {
        if (level > 0 && level <= NUM_LEVELS) {
            return pointsPerLevel[level - 1];
        }
        return 0;
    }

    public void setTotalPoints(int points) {
        totalPoints = points;
    }

    public void addPoints(int points, int level) {
        if (level > 0 && level <= NUM_LEVELS) {
            pointsPerLevel[level - 1] += points;
            totalPoints += points;
            updateMainActivityUI();
        }
    }

    public String getTotalPointsAsString() {
        return String.valueOf(totalPoints);
    }

    public void updateMainActivityUI() {
        if (mainActivity != null) {
            mainActivity.updatePointsTextView();
        }
    }

    public void setMainActivity(MainActivity activity) {
        this.mainActivity = activity;
    }
}
