package client;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import static client.AchievementConstants.Names;
import static client.AchievementConstants.Status;
import static client.AchievementConstants.Rewards;


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
        achievements.add(new Achievement(Names.FIRST_LOGIN, Status.DONE, Rewards.NX_3000));
        achievements.add(new Achievement(Names.REACH_LEVEL_30, Status.UNDONE, Rewards.MESOS_1M));
        achievements.add(new Achievement(Names.REACH_LEVEL_120, Status.UNDONE, Rewards.LEVEL_1_START_EXTRA_BUFFS));
        achievements.add(new Achievement(Names.REACH_LEVEL_200, Status.UNDONE, Rewards.FAME_420_START));
        achievements.add(new Achievement(Names.LEVEL_200, Status.UNDONE, Rewards.ASCENSION_UNLOCK));
        return achievements;
    }

    // Method to generate initial JSON structure
    public static String getInitialAchievementsJson() {
        return String.format("""
            {
                "achievements": %s,
                "ascension": [],
                "autoStoreOnLoot": true
            }
            """, new ObjectMapper().valueToTree(getInitialAchievements()).toString());
    }

}
