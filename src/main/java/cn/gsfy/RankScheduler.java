package cn.gsfy;

import cn.nukkit.Player;
import cn.nukkit.event.Listener;
import cn.nukkit.utils.TextFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RankScheduler implements Listener {

    private final RankMain plugin;
    private int taskId = -1;

    public RankScheduler(RankMain plugin) {
        this.plugin = plugin;
    }

    public void startSchedulers() {
        this.taskId = plugin.getServer().getScheduler().scheduleDelayedRepeatingTask(plugin,
                this::checkExpiredRanks, 20, 20).getTaskId();
    }

    public void stopSchedulers() {
        if (taskId != -1) {
            plugin.getServer().getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    public void applyActiveRank(Player player) {
        String playerName = player.getName();
        if (!plugin.getPlayerRanks().exists(playerName)) {
            player.setNameTag(playerName);
            return;
        }

        Map<String, Object> data = plugin.getPlayerRanks().getSection(playerName);
        String activeRank = (String) data.get("active_rank");

        if (activeRank != null && plugin.getBaseRanks().exists(activeRank)) {
            String rankText = plugin.getBaseRanks().getString(activeRank);
            player.setNameTag("[" + rankText + "§r] " + playerName);
        } else {
            player.setNameTag(playerName);
        }
    }

    private void checkExpiredRanks() {
        long currentTime = System.currentTimeMillis();
        boolean modified = false;

        for (String playerName : plugin.getPlayerRanks().getKeys(false)) {
            Map<String, Object> playerData = plugin.getPlayerRanks().getSection(playerName);
            Object ranksObj = playerData.get("available_ranks");

            if (!(ranksObj instanceof List<?>)) {
                continue;
            }

            List<Map<String, Object>> availableRanks = new java.util.ArrayList<>();
            boolean playerModified = false;
            String activeRankToClear = null;

            for (Object item : (List<?>) ranksObj) {
                if (!(item instanceof Map<?, ?>)) {
                    continue;
                }

                Map<String, Object> rankData = new HashMap<>();
                for (Map.Entry<?, ?> entry : ((Map<?, ?>) item).entrySet()) {
                    if (entry.getKey() instanceof String) {
                        rankData.put((String) entry.getKey(), entry.getValue());
                    }
                }

                Object rankNameObj = rankData.get("rank");
                Object expireTimeObj = rankData.get("expire_time");

                if (!(rankNameObj instanceof String rankName)) {
                    continue;
                }

                long expireTime = -1;
                if (expireTimeObj instanceof Number) {
                    expireTime = ((Number) expireTimeObj).longValue();
                }

                if (expireTime != -1 && expireTime <= currentTime) {
                    Object activeRank = playerData.get("active_rank");
                    if (rankName.equals(activeRank)) {
                        activeRankToClear = rankName;
                    }

                    playerModified = true;
                    modified = true;
                    continue;
                }

                availableRanks.add(rankData);
            }

            if (playerModified) {
                playerData.put("available_ranks", availableRanks);

                if (activeRankToClear != null) {
                    playerData.put("active_rank", null);
                    Player player = plugin.getServer().getPlayerExact(playerName);
                    if (player != null) {
                        player.setNameTag(playerName);
                        String rankDisplayName = plugin.getBaseRanks().getString(activeRankToClear);
                        player.sendMessage(TextFormat.RED + "Your rank " + TextFormat.RESET +
                                (rankDisplayName != null ? rankDisplayName : activeRankToClear) +
                                TextFormat.RED + " has expired and been removed!");
                    }
                }

                plugin.getPlayerRanks().set(playerName, playerData);
            }
        }

        if (modified) {
            plugin.getPlayerRanks().save();
            plugin.getLogger().info("Cleaned up expired ranks and saved changes.");
        }
    }

    public String formatTime(long seconds) {
        if (seconds < 60) return seconds + " secs";
        if (seconds < 3600) return seconds / 60 + " mins";
        if (seconds < 86400) return seconds / 3600 + " hours";
        if (seconds < 2592000) return seconds / 86400 + " days";
        return seconds / 2592000 + " mons";
    }
}