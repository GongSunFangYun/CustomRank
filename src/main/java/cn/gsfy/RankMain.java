package cn.gsfy;

import cn.nukkit.Player;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class RankMain extends PluginBase {

    private Config playerRanks;
    private Config baseRanks;
    private RankCommand rankCommand;
    private RankScheduler rankScheduler;
    private RankVarRegister rankVarRegister;

    @Override
    public void onEnable() {
        getLogger().info("");
        getLogger().info(TextFormat.GOLD + "||" + TextFormat.GREEN + " CustomRank Plugin");
        getLogger().info(TextFormat.GOLD + "||" + TextFormat.GREEN + " Author: " + TextFormat.YELLOW + "GongSunFangYun");
        getLogger().info(TextFormat.GOLD + "||" + TextFormat.GREEN + " Version: " + TextFormat.BLUE + "1.6.8");
        getLogger().info("");
        getLogger().info(TextFormat.GREEN + "Plugin successfully enabled!");

        File dataFolder = this.getDataFolder();
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            getLogger().error("Failed to create data folder!");
            return;
        }

        this.playerRanks = new Config(new File(dataFolder, "player_ranks.json"), Config.JSON);
        this.baseRanks = new Config(new File(dataFolder, "base_ranks.json"), Config.JSON);
        
        this.rankCommand = new RankCommand(this);
        this.rankScheduler = new RankScheduler(this);
        this.rankVarRegister = new RankVarRegister(this);

        this.getServer().getPluginManager().registerEvents(rankCommand, this);
        this.getServer().getPluginManager().registerEvents(rankScheduler, this);
        this.getServer().getCommandMap().register("rank", rankCommand);

        if (baseRanks.getAll().isEmpty()) {
            baseRanks.save();
        }

        migrateOldData();

        for (Player player : getServer().getOnlinePlayers().values()) {
            rankScheduler.applyActiveRank(player);
        }

        rankScheduler.startSchedulers();
        rankVarRegister.registerVariables();
    }

    @Override
    public void onDisable() {
        if (rankScheduler != null) {
            rankScheduler.stopSchedulers();
        }
        getLogger().info(TextFormat.RED + "Plugin successfully disabled!");
    }

    private void migrateOldData() {
        boolean needsSave = false;
        for (String playerName : playerRanks.getKeys(false)) {
            Map<String, Object> playerData = playerRanks.getSection(playerName);

            Object oldRanksObj = playerData.get("available_ranks");
            if (oldRanksObj instanceof java.util.List<?> oldRanks) {
                if (!oldRanks.isEmpty() && oldRanks.getFirst() instanceof String) {
                    java.util.List<Map<String, Object>> newRanks = new java.util.ArrayList<>();
                    for (Object rankObj : oldRanks) {
                        if (rankObj instanceof String) {
                            Map<String, Object> rankData = new HashMap<>();
                            rankData.put("rank", rankObj);
                            rankData.put("expire_time", -1);
                            newRanks.add(rankData);
                        }
                    }
                    playerData.put("available_ranks", newRanks);
                    playerRanks.set(playerName, playerData);
                    needsSave = true;
                }
            }
        }
        if (needsSave) {
            playerRanks.save();
        }
    }

    public Config getPlayerRanks() {
        return playerRanks;
    }

    public Config getBaseRanks() {
        return baseRanks;
    }

    public RankCommand getRankCommand() {
        return rankCommand;
    }

    public RankScheduler getRankScheduler() {
        return rankScheduler;
    }

    public RankVarRegister getRankVarRegister() {
        return rankVarRegister;
    }
}