package cn.gsfy;

import cn.nukkit.Player;
import tip.utils.variables.BaseVariable;

import java.util.Map;
import java.util.HashMap;

public class RankVariable extends BaseVariable {
    private static final Map<String, String> rankCache = new HashMap<>();

    public RankVariable(Player player) {
        super(player);
        addStrReplaceString("{cusrank}", getPlayerRank(player));
    }

    @Override
    public void strReplace() {
        Player player = this.player;
        if (player != null) {
            addStrReplaceString("{cusrank}", getPlayerRank(player));
        }
    }

    private String getPlayerRank(Player player) {
        if (player == null) return "";
        String playerName = player.getName();
        return rankCache.getOrDefault(playerName, "");
    }

    public static void updatePlayerRank(String playerName, String rankId, RankMain plugin) {
        try {
            Map<String, Object> baseRanks = plugin.getBaseRanks().getAll();
            Object displayName = baseRanks.get(rankId);
            if (displayName != null) {
                rankCache.put(playerName, displayName.toString());
            } else {
                rankCache.put(playerName, rankId);
            }
            plugin.getLogger().debug("Updated rank for " + playerName + ": " + rankCache.get(playerName));
        } catch (Exception e) {
            plugin.getLogger().error("Failed to update player rank: " + e.getMessage());
        }
    }

    public static void clearPlayerRank(String playerName) {
        rankCache.put(playerName, "");
    }

    public static void reloadRankCache(RankMain plugin) {
        rankCache.clear();

        try {
            Map<String, Object> baseRanks = plugin.getBaseRanks().getAll();
            Map<String, Object> playerData = plugin.getPlayerRanks().getAll();

            for (Map.Entry<String, Object> entry : playerData.entrySet()) {
                String playerName = entry.getKey();

                if (entry.getValue() instanceof Map<?, ?> playerInfo) {

                    Object activeRankObj = playerInfo.get("active_rank");
                    if (activeRankObj instanceof String rankId) {

                        Object displayName = baseRanks.get(rankId);
                        if (displayName != null) {
                            rankCache.put(playerName, displayName.toString());
                        } else {
                            rankCache.put(playerName, rankId);
                        }
                    }
                }
            }

            plugin.getLogger().info("Loaded " + rankCache.size() + " player ranks");
        } catch (Exception e) {
            plugin.getLogger().error("Failed to load rank data: " + e.getMessage());
        }
    }
}