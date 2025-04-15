package client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class AccountExtraDetails {
    private List<Achievement> achievements;
    private List<String> ascension;
    private boolean autoStoreOnLoot = true;
    @JsonProperty("dailyQuest")// Map JSON key "daily_quest" to this field
    private DailyQuest dailyQuest;

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

    public boolean shouldAutoStoreOnLoot() {
        return autoStoreOnLoot;
    }

    public void setAutoStoreOnLoot(boolean autoStoreOnLoot) {
        this.autoStoreOnLoot = autoStoreOnLoot;
    }

    public DailyQuest getDailyQuest() {
        return dailyQuest;
    }

    public void setDailyQuest(DailyQuest dailyQuest) {
        this.dailyQuest = dailyQuest;
    }
}