package me.Gabrielhulk10.odisseaBountySystem;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BountyCommand implements CommandExecutor, TabCompleter {

    private OdisseaBountySystem plugin;

    public BountyCommand(OdisseaBountySystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage(plugin.getConfig().getString("messages.usage"));
            return true;
        }

        if (args[0].equals("list")) {
            for (String player : plugin.bounties.keySet()) {
                sender.sendMessage(player + " - " + plugin.bounties.get(player).getAmount());
            }
            return true;
        }

        if (args[0].equals("remove")) {
            if (!sender.hasPermission("bounty.remove")) {
                sender.sendMessage(plugin.getConfig().getString("messages.no-permission"));
                return true;
            }
            plugin.bounties.remove(args[1]);
            plugin.getConfig().set("bounties." + args[1], null);
            plugin.saveConfig();
            sender.sendMessage("You removed the bounty on " + args[1]);
            return true;
        }

        if (args[0].equals("menu")) {
            Player player = (Player) sender;
            new BountyGUI(plugin).openMenu(player);
            return true;
        }

        if (args[0].equals("group")) {
            if (args.length < 2) {
                sender.sendMessage("Usage: /bounty group <create|invite|accept>");
                return true;
            }
        }

        if (args[0].equals("group") && args[1].equals("create")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getConfig().getString("messages.player-only"));
                return true;
            }
            Player player = (Player) sender;
            String leader = player.getName();
            if (plugin.groups.containsKey(leader)) {
                sender.sendMessage(plugin.getConfig().getString("messages.group-exists"));
                return true;
            }
            plugin.groups.put(leader, new BountyGroup(leader));
            sender.sendMessage("Group created!");
            return true;
        }

        if (args[0].equals("group") && args[1].equals("invite") && args.length == 3) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getConfig().getString("messages.player-only"));
                return true;
            }
            Player player = (Player) sender;
            String leader = player.getName();
            if (!plugin.groups.containsKey(leader)) {
                sender.sendMessage(plugin.getConfig().getString("messages.no-group"));
                return true;
            }
            if (plugin.groups.get(leader).getMembers().size() == 5) {
                sender.sendMessage(plugin.getConfig().getString("messages.group-full"));
                return true;
            }
            if (plugin.groups.get(leader).getMembers().contains(args[2])) {
                sender.sendMessage("This player has already been invited!");
                return true;
            }
            plugin.groups.get(leader).getInvites().add(args[2]);
            Player invited = Bukkit.getPlayer(args[2]);
            if (invited != null) {
                invited.sendMessage("You have been invited to " + leader + "'s group! Type /bounty group accept to join.");
            }
            sender.sendMessage("You invited " + args[2] + " to the group!");
            return true;
        }

        if (args[0].equals("group") && args[1].equals("accept")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getConfig().getString("messages.player-only"));
                return true;
            }
            Player player = (Player) sender;
            BountyGroup foundGroup = null;
            for (BountyGroup group : plugin.groups.values()) {
                if (group.getInvites().contains(player.getName())) {
                    foundGroup = group;
                    break;
                }
            }
            if (foundGroup == null) {
                sender.sendMessage("You have no pending invites!");
                return true;
            }
            foundGroup.getMembers().add(player.getName());
            foundGroup.getInvites().remove(player.getName());
            sender.sendMessage("You joined the group!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(plugin.getConfig().getString("messages.usage"));
            return true;
        }

        String target = args[0];
        Player player = (Player) sender;

        if (player.getName().equals(target)) {
            sender.sendMessage(plugin.getConfig().getString("messages.not-yourself"));
            return true;
        }

        if (!Bukkit.getOfflinePlayer(target).hasPlayedBefore()) {
            sender.sendMessage("This player has never played on the server!");
            return true;
        }

        TownyAPI api = TownyAPI.getInstance();
        Resident residentPlayer = api.getResident(player.getName());
        Resident residentTarget = api.getResident(target);

        if (residentPlayer.hasTown() && residentTarget.hasTown()) {
            if (residentPlayer.getTownOrNull().equals(residentTarget.getTownOrNull())) {
                sender.sendMessage("You can't place a bounty on a member of your town!");
                return true;
            }
        }

        if (residentPlayer.hasNation() && residentTarget.hasNation()) {
            if (residentPlayer.getNationOrNull().equals(residentTarget.getNationOrNull())) {
                sender.sendMessage("You can't place a bounty on a member of your nation!");
                return true;
            }
        }

        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("The amount must be a number!");
            return true;
        }

        if (plugin.economy.getBalance(player) < amount) {
            sender.sendMessage(plugin.getConfig().getString("messages.not-enough-money"));
            return true;
        }

        plugin.economy.withdrawPlayer(player, amount);
        String hunter = args.length >= 3 ? args[2] : null;

        if (plugin.bounties.containsKey(target)) {
            int oldAmount = plugin.bounties.get(target).getAmount();
            plugin.bounties.put(target, new Bounty(player.getName(), target, oldAmount + amount, hunter));
        } else {
            plugin.bounties.put(target, new Bounty(player.getName(), target, amount, hunter));
        }

        plugin.getConfig().set("bounties." + target, plugin.bounties.get(target).getAmount());
        plugin.saveConfig();
        sender.sendMessage("Bounty placed on " + target + " for " + amount + " coins!");
        plugin.getServer().broadcastMessage("⚠ §cA bounty of " + amount + " coins has been placed on " + target + "!");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (strings.length == 1) {
            return List.of("list", "remove", "menu", "group");
        }
        if (strings.length == 2 && strings[0].equals("remove")) {
            return new ArrayList<>(plugin.bounties.keySet());
        }
        if (strings.length == 2 && strings[0].equals("list")) {
            return new ArrayList<>(plugin.bounties.keySet());
        }
        if (strings.length == 2 && strings[0].equals("group")) {
            return List.of("create", "invite", "accept");
        }
        if (strings.length == 3 && strings[1].equals("invite")) {
            List<String> onlinePlayers = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                onlinePlayers.add(p.getName());
            }
            return onlinePlayers;
        }
        return List.of();
    }
}