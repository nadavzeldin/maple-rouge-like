package client;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

// Class for achievement objects
public class Achievement {
    private String name;
    private String status;
    private String bonus;

    public Achievement() {
    }

    public Achievement(String name, String status, String bonus) {
        this.name = name;
        this.status = status;
        this.bonus = bonus;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBonus() {
        return bonus;
    }

    public void setBonus(String bonus) {
        this.bonus = bonus;
    }
    public static List<Achievement> getInitialAchievements() {
        List<Achievement> achievements = new ArrayList<>();
        achievements.add(new Achievement("First Login", "done", "3,000 NX"));
        achievements.add(new Achievement("First time reach 30", "undone", "+5 Speed (Account-wide)"));
        achievements.add(new Achievement("First time reach 60", "undone", "+5 Speed (Account-wide)"));
        achievements.add(new Achievement("First time reach 90", "undone", "+10 Jump (Account-wide)"));
        achievements.add(new Achievement("First time reach 120", "undone", "+10 Jump (Account-wide)"));
        achievements.add(new Achievement("First time reach 200", "undone", "+20 Speed, +20 Jump (Account-wide)"));
        achievements.add(new Achievement("Reach Level 200", "undone", "Unlock Ascension System"));
        return achievements;
    }

    // Method to generate initial JSON structure
    public static String getInitialAchievementsJson() {
        return String.format("""
            {
                "achievements": %s,
                "ascension": []
            }
            """, new ObjectMapper().valueToTree(getInitialAchievements()).toString());
    }

}
