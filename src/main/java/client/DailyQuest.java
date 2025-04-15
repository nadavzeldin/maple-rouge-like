package client;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DailyQuest {
    private String date;
    @JsonProperty("monster_id")
    private int monsterId;
    @JsonProperty("kill_count") // Map JSON key "kill_count" to killCount field
    private int killCount;
    private boolean completed;

    // Constructor
    public DailyQuest() {
        this.killCount = 0;
        this.completed = false;
    }

    // Getters and setters
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getMonsterId() {
        return monsterId;
    }

    public void setMonsterId(int monsterId) {
        this.monsterId = monsterId;
    }

    public int getKillCount() {
        return killCount;
    }

    public void setKillCount(int killCount) {
        this.killCount = killCount;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}