package client;

import java.util.List;

public class AccountExtraDetails {
    private List<Achievement> achievements;
    private List<String> ascension;
    private boolean autoStoreOnLoot = true;

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

    public int AscensionCount() {
        if (ascension == null || ascension.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (String item : ascension) {
            if (item.startsWith(AscensionConstants.Names.INFINITE)) {
                // Extract the number from the "Infinite(X)" format
                int startIndex = item.indexOf('(');
                int endIndex = item.indexOf(')');
                if (startIndex != -1 && endIndex != -1) {
                    try {
                        int infiniteValue = Integer.parseInt(item.substring(startIndex + 1, endIndex));
                        count += infiniteValue;
                    } catch (NumberFormatException e) {
                        // If parsing fails, just count it as one
                        count += 1;
                    }
                } else {
                    // If format is incorrect, just count it as one
                    count += 1;
                }
            } else {
                // Regular ascension
                count += 1;
            }
        }

        return count;
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
}

