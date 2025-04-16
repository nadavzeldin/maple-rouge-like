package client;

import java.util.HashMap;
import java.util.Map;
import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class CharacterSettings implements Serializable {
    // This field will be directly serialized/deserialized by Jackson
    private Map<Integer, Integer> rewardStatus;

    // Default constructor for Jackson
    public CharacterSettings() {
        initializeRewardStatus();
    }

    // Getters and setters
    public Map<Integer, Integer> getRewardStatus() {
        if (rewardStatus == null) {
            initializeRewardStatus();
        }
        return rewardStatus;
    }

    // This setter is needed for Jackson to set the entire map
    public void setRewardStatus(Map<Integer, Integer> rewardStatus) {
        this.rewardStatus = rewardStatus;
    }

    // This is a utility method, not for Jackson
    @JsonIgnore
    public void setRewardForLevel(Integer rewardLevel) {
        if (rewardStatus == null) {
            initializeRewardStatus();
        }
        this.rewardStatus.put(rewardLevel, 1);
    }

    public boolean canTakeReward(Integer rewardLevel) {
        if (rewardStatus == null) {
            initializeRewardStatus();
        }

        // If the reward doesn't exist in the map or is not set to 1 (claimed),
        // then the player can take it
        return !rewardStatus.containsKey(rewardLevel) || rewardStatus.get(rewardLevel) != 1;
    }

    public void initializeRewardStatus() {
        if (this.rewardStatus == null) {
            this.rewardStatus = new HashMap<>();
        }
    }
}