package cn.gsfy;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.utils.TextFormat;

import java.util.*;

@SuppressWarnings("SameReturnValue")
public class RankCommand extends Command implements Listener {

    private final RankMain plugin;

    public RankCommand(RankMain plugin) {
        super("rank", "Rank management command", "/rank help", new String[]{"title"});
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                RankForm.showRankSelectionForm((Player) sender, plugin);
            } else {
                sender.sendMessage("§6|| §bCustom§3Rank §9v1.6.8");
                sender.sendMessage("§6|| §aUse /rank help to get help.");
            }
            return true;
        }

        return switch (args[0].toLowerCase()) {
            case "help" -> handleHelp(sender);
            case "give" -> checkPermissionAndExecute(sender, "rank.give", this::handleGiveRank, args);
            case "take" -> checkPermissionAndExecute(sender, "rank.take", this::handleTakeRank, args);
            case "create" -> checkPermissionAndExecute(sender, "rank.create", this::handleCreateRank, args);
            case "delete" -> checkPermissionAndExecute(sender, "rank.delete", this::handleDeleteRank, args);
            case "use" -> checkPermissionAndExecute(sender, "rank.use", this::handleUseRank, args);
            case "clear" -> checkPermissionAndExecute(sender, "rank.clear", this::handleClearRank, args);
            case "listall" -> checkPermissionAndExecute(sender, "rank.listall", this::handleListAllRanks, args);
            case "check" -> checkPermissionAndExecute(sender, "rank.check", this::handleCheckRank, args);
            default -> {
                sender.sendMessage(TextFormat.RED + "Unknown command. Use /rank help for help.");
                yield true;
            }
        };
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getRankScheduler().applyActiveRank(player);
    }

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        String message = event.getMessage();

        String rankPrefix = "";
        if (plugin.getPlayerRanks().exists(playerName)) {
            Map<String, Object> data = plugin.getPlayerRanks().getSection(playerName);
            String activeRank = (String) data.get("active_rank");
            if (activeRank != null && plugin.getBaseRanks().exists(activeRank)) {
                rankPrefix = "[" + plugin.getBaseRanks().getString(activeRank) + "§r] ";
            }
        }

        event.setFormat(rankPrefix + playerName + ": " + message);
    }

    private boolean checkPermissionAndExecute(CommandSender sender, String permission,
                                              CommandHandler handler, String[] args) {
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(TextFormat.RED + "You don't have permission to use this command!");
            return true;
        }
        return handler.handle(sender, args);
    }

    private boolean handleHelp(CommandSender sender) {
        sender.sendMessage(TextFormat.BLUE + "===== Rank Command Help =====" + TextFormat.RESET);
        sender.sendMessage(TextFormat.WHITE + "[" + TextFormat.RED + "Admin+" + TextFormat.WHITE + "] " + TextFormat.GOLD + "/rank " + TextFormat.BLUE + "give " + TextFormat.GREEN + "<player_name> <@p|@r|@s> <baseID>" + TextFormat.WHITE + " - Give rank to a player");
        sender.sendMessage(TextFormat.WHITE + "[" + TextFormat.RED + "Admin+" + TextFormat.WHITE + "] " + TextFormat.GOLD + "/rank " + TextFormat.BLUE + "take " + TextFormat.GREEN + "<player_name> <@p|@r|@s> <baseID>" + TextFormat.WHITE + " - Take rank from a player");
        sender.sendMessage(TextFormat.WHITE + "[" + TextFormat.RED + "Admin+" + TextFormat.WHITE + "] " + TextFormat.GOLD + "/rank " + TextFormat.BLUE + "create " + TextFormat.GREEN + "<baseID> <display_name>" + TextFormat.WHITE + " - Add rank to the library");
        sender.sendMessage(TextFormat.WHITE + "[" + TextFormat.RED + "Admin+" + TextFormat.WHITE + "] " + TextFormat.GOLD + "/rank " + TextFormat.BLUE + "delete " + TextFormat.GREEN + "<baseID>" + TextFormat.WHITE + " - Delete rank from the library");
        sender.sendMessage(TextFormat.WHITE + "[" + TextFormat.RED + "Admin+" + TextFormat.WHITE + "] " + TextFormat.GOLD + "/rank " + TextFormat.BLUE + "listall" + TextFormat.WHITE + " - List all ranks");
        sender.sendMessage(TextFormat.WHITE + "[" + TextFormat.RED + "Admin+" + TextFormat.WHITE + "] " + TextFormat.GOLD + "/rank " + TextFormat.BLUE + "check " + TextFormat.GREEN + "<player_name> <<@p|@r|@s>" + TextFormat.WHITE + " - Check player's ranks");
        sender.sendMessage(TextFormat.WHITE + "[" + TextFormat.LIGHT_PURPLE + "Player" + TextFormat.WHITE + "] " + TextFormat.GOLD + "/rank " + TextFormat.WHITE + " - Open rank selection window");
        sender.sendMessage(TextFormat.WHITE + "[" + TextFormat.LIGHT_PURPLE + "Player" + TextFormat.WHITE + "] " + TextFormat.GOLD + "/rank " + TextFormat.BLUE + "use " + TextFormat.GREEN + "<baseID>" + TextFormat.WHITE + " - Use a rank that you have");
        sender.sendMessage(TextFormat.WHITE + "[" + TextFormat.LIGHT_PURPLE + "Player" + TextFormat.WHITE + "] " + TextFormat.GOLD + "/rank " + TextFormat.BLUE + "clear" + TextFormat.WHITE + " - Clear the rank that is now in use");
        sender.sendMessage(TextFormat.WHITE + "[" + TextFormat.LIGHT_PURPLE + "Player" + TextFormat.WHITE + "] " + TextFormat.GOLD + "/rank " + TextFormat.BLUE + "help" + TextFormat.WHITE + " - Show this help");
        return true;
    }

    private boolean handleGiveRank(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(TextFormat.RED + "Usage: §6/rank give <PLAYER_NAME|@p|@r|@s> <BASE_ID> <VALID_DURATION>");
            sender.sendMessage(TextFormat.GRAY + "Duration format: 1m(Minutes), 1h(Hours), 1d(Days), 1y(Years)");
            return true;
        }

        String targetPartial = args[1];
        String rankName = args[2];
        long expireTime = -1;

        if (args.length >= 4) {
            try {
                String durationStr = args[3];
                char unit = durationStr.charAt(durationStr.length() - 1);
                long value = Long.parseLong(durationStr.substring(0, durationStr.length() - 1));

                switch (unit) {
                    case 'm': expireTime = System.currentTimeMillis() + value * 60 * 1000; break;
                    case 'h': expireTime = System.currentTimeMillis() + value * 60 * 60 * 1000; break;
                    case 'd': expireTime = System.currentTimeMillis() + value * 24 * 60 * 60 * 1000; break;
                    case 'y': expireTime = System.currentTimeMillis() + value * 365L * 24 * 60 * 60 * 1000; break;
                    default:
                        sender.sendMessage(TextFormat.RED + "Invalid duration unit! Use m / h / d / y.");
                        return true;
                }
            } catch (Exception e) {
                sender.sendMessage(TextFormat.RED + "Invalid duration format! Example: 1d, 2h, 30m.");
                return true;
            }
        }

        if (!plugin.getBaseRanks().exists(rankName)) {
            sender.sendMessage(TextFormat.RED + "This rank does not exist!");
            return true;
        }

        if (targetPartial.startsWith("@")) {
            List<Player> targets = parseSelector(targetPartial, sender);
            if (targets.isEmpty()) {
                sender.sendMessage(TextFormat.RED + "No matching players found for selector: " + targetPartial);
                return true;
            }

            for (Player targetPlayer : targets) {
                giveRankToPlayer(targetPlayer.getName(), rankName, expireTime, sender, args);
            }
            return true;
        }

        String target = findBestPlayerMatch(targetPartial);
        if (target == null) {
            target = targetPartial;
        }

        return giveRankToPlayer(target, rankName, expireTime, sender, args);
    }

    private boolean handleTakeRank(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(TextFormat.RED + "Usage: §6/rank take <PLAYER_NAME|@p|@r|@s> <BASE_ID>");
            return true;
        }

        String targetPartial = args[1];
        String rankName = args[2];

        if (targetPartial.startsWith("@")) {
            List<Player> targets = parseSelector(targetPartial, sender);
            if (targets.isEmpty()) {
                sender.sendMessage(TextFormat.RED + "No matching players found for selector: " + targetPartial);
                return true;
            }

            boolean success = false;
            for (Player targetPlayer : targets) {
                success |= takeRankFromPlayer(targetPlayer.getName(), rankName, sender);
            }
            return success;
        }

        String target = findBestPlayerMatch(targetPartial);
        if (target == null) {
            target = targetPartial;
        }

        return takeRankFromPlayer(target, rankName, sender);
    }

    private boolean handleCreateRank(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(TextFormat.RED + "Usage: §6/rank create <BASE_ID> <DISPLAY_TEXT>");
            return true;
        }

        String rankName = args[1];
        String displayText = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        if (plugin.getBaseRanks().exists(rankName)) {
            sender.sendMessage(TextFormat.RED + "This rank baseID already exists!");
            return true;
        }

        plugin.getBaseRanks().set(rankName, displayText);
        plugin.getBaseRanks().save();
        sender.sendMessage(TextFormat.GREEN + "Successfully created new rank: " + TextFormat.RESET + displayText);
        return true;
    }

    private boolean handleDeleteRank(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(TextFormat.RED + "Usage: §6/rank delete <BASE_ID>");
            return true;
        }

        String rankName = args[1];

        if (!plugin.getBaseRanks().exists(rankName)) {
            sender.sendMessage(TextFormat.RED + "This rank does not exist!");
            return true;
        }

        boolean modified = false;
        for (String playerName : plugin.getPlayerRanks().getKeys(false)) {
            Map<String, Object> playerData = plugin.getPlayerRanks().getSection(playerName);
            Object ranksObj = playerData.get("available_ranks");

            if (!(ranksObj instanceof List<?> tempList)) {
                continue;
            }

            List<Map<String, Object>> availableRanks = new ArrayList<>();

            for (Object item : tempList) {
                if (item instanceof Map<?, ?> rawMap) {
                    Map<String, Object> safeMap = new HashMap<>();

                    for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                        if (entry.getKey() instanceof String) {
                            safeMap.put((String) entry.getKey(), entry.getValue());
                        }
                    }
                    availableRanks.add(safeMap);
                }
            }

            if (availableRanks.isEmpty()) {
                continue;
            }

            Iterator<Map<String, Object>> iterator = availableRanks.iterator();
            boolean playerModified = false;

            while (iterator.hasNext()) {
                Map<String, Object> rankData = iterator.next();
                Object rankObj = rankData.get("rank");

                if (rankObj instanceof String && rankName.equals(rankObj)) {
                    iterator.remove();
                    playerModified = true;

                    Object activeRankObj = playerData.get("active_rank");
                    if (rankName.equals(activeRankObj)) {
                        playerData.put("active_rank", null);
                        Player player = plugin.getServer().getPlayerExact(playerName);
                        if (player != null) {
                            player.setNameTag(playerName);
                        }
                    }
                }
            }

            if (playerModified) {
                playerData.put("available_ranks", availableRanks);
                plugin.getPlayerRanks().set(playerName, playerData);
                modified = true;
            }
        }

        plugin.getBaseRanks().remove(rankName);
        plugin.getBaseRanks().save();

        if (modified) {
            plugin.getPlayerRanks().save();
        }

        sender.sendMessage(TextFormat.GREEN + "Successfully deleted rank: " + TextFormat.RESET + rankName);
        return true;
    }

    public boolean handleUseRank(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextFormat.RED + "Only players can use this command.");
            return true;
        }

        if (args.length < 2) {
            RankForm.showRankSelectionForm(player, plugin);
            return true;
        }

        String playerName = player.getName();
        String rankName = args[1];

        if (!plugin.getPlayerRanks().exists(playerName)) {
            sender.sendMessage(TextFormat.RED + "You don't have any ranks!");
            return true;
        }

        Map<String, Object> playerData = plugin.getPlayerRanks().getSection(playerName);
        Object ranksObj = playerData.get("available_ranks");

        if (!(ranksObj instanceof List<?> tempList)) {
            sender.sendMessage(TextFormat.RED + "Invalid rank data format!");
            return true;
        }

        boolean hasRank = false;
        boolean isExpired = false;
        long currentTime = System.currentTimeMillis();

        for (Object item : tempList) {
            if (!(item instanceof Map<?, ?> rankData)) {
                continue;
            }

            Object rankObj = rankData.get("rank");
            Object expireTimeObj = rankData.get("expire_time");

            if (rankObj instanceof String && rankName.equals(rankObj)) {
                hasRank = true;

                if (expireTimeObj instanceof Number) {
                    long expireTime = ((Number) expireTimeObj).longValue();
                    if (expireTime != -1 && expireTime <= currentTime) {
                        isExpired = true;
                    }
                }
                break;
            }
        }

        if (!hasRank) {
            sender.sendMessage(TextFormat.RED + "You don't have this rank!");
            return true;
        }

        if (isExpired) {
            sender.sendMessage(TextFormat.RED + "This rank has expired!");
            return true;
        }

        playerData.put("active_rank", rankName);
        plugin.getPlayerRanks().set(playerName, playerData);
        plugin.getPlayerRanks().save();

        String rankText = plugin.getBaseRanks().getString(rankName);
        if (rankText == null) {
            rankText = rankName;
        }
        player.setNameTag("[" + rankText + "§r] " + playerName);

        RankVariable.updatePlayerRank(playerName, rankName, plugin);
        plugin.getRankVarRegister().onRankChanged(player);

        sender.sendMessage(TextFormat.GREEN + "Successfully activated rank: " + TextFormat.RESET + rankText);
        return true;
    }

    public boolean handleClearRank(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextFormat.RED + "Only players can use this command.");
            return true;
        }

        String playerName = player.getName();

        if (!plugin.getPlayerRanks().exists(playerName)) {
            sender.sendMessage(TextFormat.YELLOW + "No rank is currently active!");
            return true;
        }

        Map<String, Object> playerData = plugin.getPlayerRanks().getSection(playerName);
        String activeRank = (String) playerData.get("active_rank");

        if (activeRank == null) {
            sender.sendMessage(TextFormat.YELLOW + "No rank is currently active!");
            return true;
        }

        String rankDisplayName = plugin.getBaseRanks().getString(activeRank);
        if (rankDisplayName == null) {
            rankDisplayName = activeRank;
        }

        playerData.put("active_rank", null);
        plugin.getPlayerRanks().set(playerName, playerData);
        plugin.getPlayerRanks().save();

        player.setNameTag(playerName);

        RankVariable.clearPlayerRank(playerName);
        plugin.getRankVarRegister().onRankChanged(player);

        sender.sendMessage(TextFormat.GREEN + "Successfully cleared active rank: " + TextFormat.RESET + rankDisplayName);
        return true;
    }

    private boolean handleListAllRanks(CommandSender sender, String[] args) {
        Map<String, Object> allRanks = plugin.getBaseRanks().getAll();
        if (allRanks.isEmpty()) {
            sender.sendMessage(TextFormat.YELLOW + "No ranks available.");
            return true;
        }

        StringBuilder message = new StringBuilder(TextFormat.RED + "===== All Available Ranks =====\n");
        allRanks.forEach((baseName, displayName) -> message.append(TextFormat.GREEN)
                .append(baseName)
                .append(TextFormat.WHITE)
                .append(" --> ")
                .append(displayName)
                .append("\n"));

        sender.sendMessage(message.toString());
        return true;
    }

    private boolean handleCheckRank(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(TextFormat.RED + "Usage: §6/rank check <PLAYER_NAME|@p|@r|@s>");
            return true;
        }

        String targetPartial = args[1];

        if (targetPartial.startsWith("@")) {
            List<Player> targets = parseSelector(targetPartial, sender);
            if (targets.isEmpty()) {
                sender.sendMessage(TextFormat.RED + "No matching players found for selector: " + targetPartial);
                return true;
            }

            boolean success = false;
            for (Player targetPlayer : targets) {
                success |= checkPlayerRank(targetPlayer.getName(), sender);
            }
            return success;
        }

        String target = findBestPlayerMatch(targetPartial);
        if (target == null) {
            target = targetPartial;
        }

        return checkPlayerRank(target, sender);
    }

    private boolean checkPlayerRank(String target, CommandSender sender) {
        if (!plugin.getPlayerRanks().exists(target)) {
            sender.sendMessage(TextFormat.YELLOW + "This player does not have any ranks!");
            return true;
        }

        Map<String, Object> playerData = plugin.getPlayerRanks().getSection(target);
        Object ranksObj = playerData.get("available_ranks");

        if (!(ranksObj instanceof List<?> tempList)) {
            sender.sendMessage(TextFormat.YELLOW + "This player does not have any ranks!");
            return true;
        }

        StringBuilder message = new StringBuilder(TextFormat.GOLD + "===== " + target + "'s Ranks =====\n");
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

            String displayName = plugin.getBaseRanks().getString(rankName);
            if (displayName == null) {
                displayName = rankName;
            }

            message.append(TextFormat.GREEN).append(rankName).append(TextFormat.RESET).append(" --> ").append(displayName);

            if (expireTimeObj instanceof Number) {
                long expireTime = ((Number) expireTimeObj).longValue();
                if (expireTime != -1) {
                    long remaining = (expireTime - currentTime) / 1000;
                    if (remaining <= 0) {
                        message.append(TextFormat.RED).append(" (Expired)");
                    } else {
                        String timeLeft = plugin.getRankScheduler().formatTime(remaining);
                        message.append(TextFormat.DARK_GRAY).append(" (").append(timeLeft).append(" remaining)");
                    }
                } else {
                    message.append(TextFormat.DARK_GRAY).append(" (Permanent)");
                }
            } else {
                message.append(TextFormat.DARK_GRAY).append(" (Permanent)");
            }

            message.append("\n");
            hasValidRanks = true;
        }

        if (!hasValidRanks) {
            sender.sendMessage(TextFormat.YELLOW + "This player does not have any valid ranks!");
            return true;
        }

        String activeRank = (String) playerData.get("active_rank");
        if (activeRank != null) {
            String activeDisplay = plugin.getBaseRanks().getString(activeRank);
            message.append("\n").append(TextFormat.GOLD).append("Currently using: ")
                    .append(TextFormat.RESET).append(activeDisplay != null ? activeDisplay : activeRank);
        }

        sender.sendMessage(message.toString());
        return true;
    }

    private List<Player> parseSelector(String selector, CommandSender sender) {
        List<Player> players = new ArrayList<>();

        if (selector == null || !selector.startsWith("@")) {
            return players;
        }

        Player senderPlayer = sender instanceof Player ? (Player) sender : null;

        switch (selector.toLowerCase()) {
            case "@p":
                if (senderPlayer == null) break;

                Player nearest = null;
                double nearestDistance = Double.MAX_VALUE;

                for (Player player : plugin.getServer().getOnlinePlayers().values()) {
                    if (player == senderPlayer) continue;

                    double distance = senderPlayer.distance(player);
                    if (distance < nearestDistance) {
                        nearestDistance = distance;
                        nearest = player;
                    }
                }

                players.add(nearest != null ? nearest : senderPlayer);
                break;

            case "@r":
                List<Player> onlinePlayers = new ArrayList<>(plugin.getServer().getOnlinePlayers().values());
                if (!onlinePlayers.isEmpty()) {
                    players.add(onlinePlayers.get(new Random().nextInt(onlinePlayers.size())));
                }
                break;

            case "@s":
                if (senderPlayer != null) {
                    players.add(senderPlayer);
                }
                break;
        }

        return players;
    }

    private String findBestPlayerMatch(String partialName) {
        partialName = partialName.toLowerCase();
        List<String> matches = new ArrayList<>();

        for (Player player : plugin.getServer().getOnlinePlayers().values()) {
            String playerName = player.getName();
            if (playerName.toLowerCase().startsWith(partialName)) {
                matches.add(playerName);
            }
        }

        if (matches.size() == 1) {
            return matches.getFirst();
        }

        return null;
    }

    private boolean giveRankToPlayer(String target, String rankName, long expireTime, CommandSender sender, String[] args) {
        Map<String, Object> playerData = plugin.getPlayerRanks().exists(target) ?
                plugin.getPlayerRanks().getSection(target) : new HashMap<>();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> availableRanks = (List<Map<String, Object>>) playerData.computeIfAbsent(
                "available_ranks", k -> new ArrayList<>());

        boolean alreadyHas = false;
        for (Map<String, Object> rankData : availableRanks) {
            if (rankName.equals(rankData.get("rank"))) {
                alreadyHas = true;
                rankData.put("expire_time", expireTime);
                break;
            }
        }

        if (!alreadyHas) {
            Map<String, Object> newRank = new HashMap<>();
            newRank.put("rank", rankName);
            newRank.put("expire_time", expireTime);
            availableRanks.add(newRank);
        }

        playerData.put("available_ranks", availableRanks);
        plugin.getPlayerRanks().set(target, playerData);
        plugin.getPlayerRanks().save();

        String durationMsg = expireTime == -1 ? "permanently" : TextFormat.GREEN + "for " + TextFormat.GOLD + args[3];
        sender.sendMessage(TextFormat.GREEN + "Added " +
                TextFormat.RESET + plugin.getBaseRanks().getString(rankName) +
                TextFormat.GREEN + " to " +
                TextFormat.YELLOW + target + " " +
                TextFormat.GOLD + durationMsg);
        return true;
    }

    private boolean takeRankFromPlayer(String target, String rankName, CommandSender sender) {
        if (!plugin.getPlayerRanks().exists(target)) {
            sender.sendMessage(TextFormat.RED + "This player does not have any ranks!");
            return true;
        }

        Map<String, Object> playerData = plugin.getPlayerRanks().getSection(target);
        Object ranksObj = playerData.get("available_ranks");

        if (!(ranksObj instanceof List<?> tempList)) {
            sender.sendMessage(TextFormat.RED + "The player does not have any ranks!");
            return true;
        }

        List<Map<String, Object>> availableRanks = new ArrayList<>();
        boolean removed = false;

        for (Object item : tempList) {
            if (item instanceof Map<?, ?> rawMap) {
                Map<String, Object> safeMap = new HashMap<>();

                for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                    if (entry.getKey() instanceof String) {
                        safeMap.put((String) entry.getKey(), entry.getValue());
                    }
                }

                Object rankObj = safeMap.get("rank");
                if (rankName.equals(rankObj)) {
                    removed = true;

                    Object activeRankObj = playerData.get("active_rank");
                    if (rankName.equals(activeRankObj)) {
                        playerData.put("active_rank", null);
                        Player player = plugin.getServer().getPlayerExact(target);
                        if (player != null) {
                            player.setNameTag(target);
                        }
                    }
                    continue;
                }

                availableRanks.add(safeMap);
            }
        }

        if (!removed) {
            sender.sendMessage(TextFormat.RED + "The player does not have this rank!");
            return true;
        }

        playerData.put("available_ranks", availableRanks);
        plugin.getPlayerRanks().set(target, playerData);
        plugin.getPlayerRanks().save();

        String displayName = plugin.getBaseRanks().getString(rankName);
        sender.sendMessage(TextFormat.GREEN + "Removed " + TextFormat.RESET +
                (displayName != null ? displayName : rankName) +
                TextFormat.GREEN + " rank from " + TextFormat.YELLOW + target);
        return true;
    }

    @FunctionalInterface
    private interface CommandHandler {
        boolean handle(CommandSender sender, String[] args);
    }
}