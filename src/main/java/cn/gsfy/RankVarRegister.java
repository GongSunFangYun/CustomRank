package cn.gsfy;

import cn.nukkit.Player;
import cn.nukkit.plugin.Plugin;
import java.lang.reflect.Method;

public class RankVarRegister {

    private final RankMain plugin;

    public RankVarRegister(RankMain plugin) {
        this.plugin = plugin;
    }

    public void registerVariables() {
        RankVariable.reloadRankCache(plugin);
        registerTipsVariables();
        plugin.getServer().getScheduler().scheduleDelayedRepeatingTask(
                plugin,
                () -> RankVariable.reloadRankCache(plugin),
                20 * 60 * 60,
                20 * 60 * 60
        );
    }

    @SuppressWarnings("UnnecessaryToStringCall")
    private void registerTipsVariables() {
        try {
            Plugin tipsPlugin = plugin.getServer().getPluginManager().getPlugin("Tips");
            if (tipsPlugin == null || !tipsPlugin.isEnabled()) {
                plugin.getLogger().info("Tips plugin not found, skipping variable registration");
                return;
            }

            Class<?> apiClass = Class.forName("tip.utils.Api");
            Method registerMethod = apiClass.getMethod("registerVariables", String.class, Class.class);
            registerMethod.invoke(null, "CustomRank", RankVariable.class);
            plugin.getLogger().info("Registered {cusrank} variable successfully");
        } catch (ClassNotFoundException e) {
            plugin.getLogger().info("Tips plugin API classes not found, skipping variable registration");
        } catch (NoSuchMethodException e) {
            plugin.getLogger().warning("Tips plugin API method not found: " + e.getMessage());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to register variable: " + e.toString());
        }
    }

    public void updatePlayerVariables(Player player) {
        try {
            Plugin tipsPlugin = plugin.getServer().getPluginManager().getPlugin("Tips");
            if (tipsPlugin == null || !tipsPlugin.isEnabled()) return;

            Class<?> apiClass = Class.forName("tip.utils.Api");
            Method updateMethod = apiClass.getMethod("updatePlayer", Player.class);
            updateMethod.invoke(null, player);
        } catch (Throwable ignored) {
        }
    }

    public void onRankChanged(Player player) {
        plugin.getRankScheduler().applyActiveRank(player);
        updatePlayerVariables(player);
        plugin.getLogger().debug("Rank updated for player: " + player.getName());
    }
}