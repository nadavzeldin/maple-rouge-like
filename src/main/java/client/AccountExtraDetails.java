package client;

import java.util.List;

public class AccountExtraDetails {
    private List<Achievement> achievements;
    private List<String> ascension;

    // Getters and setters
    public List<Achievement> getAchievements() {
        return achievements;
    }

    public void setAchievements(List<Achievement> achievements) {
        this.achievements = achievements;
    }

    public List<String> getAscension() {
        return ascension;
    }

    public void setAscension(List<String> ascension) {
        this.ascension = ascension;
    }
}

