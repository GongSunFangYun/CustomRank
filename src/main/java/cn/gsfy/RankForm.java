package cn.gsfy;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.handler.FormResponseHandler;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.utils.TextFormat;

import java.util.List;
import java.util.Map;

public class RankForm {

    public static void showRankSelectionForm(Player player, RankMain plugin) {
        String playerName = player.getName();

        if (!plugin.getPlayerRanks().exists(playerName)) {
            player.sendMessage(TextFormat.YELLOW + "You don't have any ranks!");
            return;
        }

        Map<String, Object> playerData = plugin.getPlayerRanks().getSection(playerName);
        Object ranksObj = playerData.get("available_ranks");

        if (!(ranksObj instanceof List<?> tempList)) {
            player.sendMessage(TextFormat.YELLOW + "You don't have any valid ranks!");
            return;
        }

        FormWindowSimple form = new FormWindowSimple("§6||§3 Rank§bManager §6||§r", "Select a rank to use.");

        form.addButton(new ElementButton(TextFormat.RED + "Clear Current Rank"));

        long currentTime = System.currentTimeMillis();
        boolean hasValidRanks = false;

        for (Object item : tempList) {
            if (!(item instanceof Map<?, ?> rankData)) {
                continue;
            }

            Object rankNameObj = rankData.get("rank");
            Object expireTimeObj = rankData.get("expire_time");

            if (!(rankNameObj instanceof String rankName)) {
                continue;
            }

            if (expireTimeObj instanceof Number) {
                long expireTime = ((Number) expireTimeObj).longValue();
                if (expireTime != -1 && expireTime <= currentTime) {
                    continue;
                }
            }

            String displayName = plugin.getBaseRanks().getString(rankName);
            if (displayName == null) {
                displayName = rankName;
            }

            String buttonText = displayName;
            if (expireTimeObj instanceof Number) {
                long expireTime = ((Number) expireTimeObj).longValue();
                if (expireTime != -1) {
                    long remaining = (expireTime - currentTime) / 1000;
                    if (remaining > 0) {
                        buttonText += TextFormat.DARK_GRAY + " \n(" + formatTime(remaining) + ")";
                    }
                } else {
                    buttonText += TextFormat.DARK_GRAY + " \n(Permanent)";
                }
            } else {
                buttonText += TextFormat.DARK_GRAY + " \n(Permanent)";
            }

            form.addButton(new ElementButton(buttonText));
            hasValidRanks = true;
        }

        if (!hasValidRanks) {
            player.sendMessage(TextFormat.YELLOW + "You don't have any valid ranks!");
            return;
        }

        form.addHandler(FormResponseHandler.withoutPlayer(ignored -> {
            if (form.wasClosed()) return;

            int buttonIndex = form.getResponse().getClickedButtonId();
            String buttonText = form.getResponse().getClickedButton().getText();

            if (buttonIndex == 0) {
                plugin.getRankCommand().handleClearRank(player, new String[0]);
                return;
            }

            String selectedRank = null;
            int rankIndex = 0;
            for (Object item : tempList) {
                if (!(item instanceof Map<?, ?> rankData)) continue;

                Object rankNameObj = rankData.get("rank");
                if (!(rankNameObj instanceof String rankName)) continue;

                Object expireTimeObj = rankData.get("expire_time");
                if (expireTimeObj instanceof Number) {
                    long expireTime = ((Number) expireTimeObj).longValue();
                    if (expireTime != -1 && expireTime <= currentTime) continue;
                }

                if (rankIndex == buttonIndex - 1) {
                    selectedRank = rankName;
                    break;
                }
                rankIndex++;
            }

            if (selectedRank != null) {
                plugin.getRankCommand().handleUseRank(player, new String[]{"use", selectedRank});
            }
        }));

        player.showFormWindow(form);
    }

    private static String formatTime(long seconds) {
        if (seconds < 60) return seconds + " secs";
        if (seconds < 3600) return seconds / 60 + " mins";
        if (seconds < 86400) return seconds / 3600 + " hours";
        if (seconds < 2592000) return seconds / 86400 + " days";
        return seconds / 2592000 + " mons";
    }
}