package me.Gabrielhulk10.odisseaBountySystem;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BountyListener implements Listener {

    private OdisseaBountySystem plugin;

    public BountyListener(OdisseaBountySystem plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player dead = event.getEntity();
        Player killer = event.getEntity().getKiller();

        if (!plugin.bounties.containsKey(dead.getName())) {
            return;
        }

        String hunter = plugin.bounties.get(dead.getName()).getHunter();

        if (killer == null) {
            return;
        }

        if (hunter != null && !hunter.equals(killer.getName())) {
            killer.sendMessage(plugin.getConfig().getString("messages.not-hunter"));
            return;
        }

        if (!killer.hasPermission("bounty.redeem")) {
            killer.sendMessage(plugin.getConfig().getString("messages.no-permission-redeem"));
            return;
        }

        int amount = plugin.bounties.get(dead.getName()).getAmount();

        BountyGroup foundGroup = null;
        for (BountyGroup group : plugin.groups.values()) {
            if (group.getMembers().contains(killer.getName())) {
                foundGroup = group;
                break;
            }
        }

        if (foundGroup != null && hunter == null) {
            int share = amount / foundGroup.getMembers().size();
            for (String member : foundGroup.getMembers()) {
                Player memberPlayer = Bukkit.getPlayer(member);
                if (memberPlayer != null) {
                    plugin.economy.depositPlayer(memberPlayer, share);
                    memberPlayer.sendMessage(plugin.getConfig().getString("messages.group-redeemed")
                            .replace("{amount}", String.valueOf(share)));
                }
            }
        } else {
            plugin.economy.depositPlayer(killer, amount);
            killer.sendMessage(plugin.getConfig().getString("messages.bounty-redeemed")
                    .replace("{player}", dead.getName())
                    .replace("{amount}", String.valueOf(amount)));
        }

        plugin.bounties.remove(dead.getName());
        plugin.getConfig().set("bounties." + dead.getName(), null);
        plugin.saveConfig();
    }

    private void openBountyList(Player player) {
        Inventory bountyList = Bukkit.createInventory(null, 54, "Bounty List");

        List<Map.Entry<String, Bounty>> sortedList = new ArrayList<>(plugin.bounties.entrySet());
        sortedList.sort((a, b) -> b.getValue().getAmount() - a.getValue().getAmount());

        int page = plugin.pages.getOrDefault(player.getName(), 0);
        int bountiesPerPage = 45;
        int start = page * bountiesPerPage;
        int end = Math.min(start + bountiesPerPage, sortedList.size());

        for (int i = start; i < end; i++) {
            Map.Entry<String, Bounty> entry = sortedList.get(i);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(entry.getKey()));
            String hunter = entry.getValue().getHunter();
            String info = hunter != null ? " [Private: " + hunter + "]" : " [Public]";
            meta.setDisplayName(entry.getKey() + " - " + entry.getValue().getAmount() + info);
            List<String> lore = new ArrayList<>();
            lore.add("§7Issued by: §f" + entry.getValue().getIssuer());
            lore.add("§7Amount: §f" + entry.getValue().getAmount());
            lore.add("§7Type: §f" + (hunter != null ? "Private" : "Public"));
            meta.setLore(lore);
            head.setItemMeta(meta);
            bountyList.addItem(head);
        }

        if (page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta meta = prev.getItemMeta();
            meta.setDisplayName("§7<< Previous page");
            prev.setItemMeta(meta);
            bountyList.setItem(45, prev);
        }

        if (end < sortedList.size()) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta meta = next.getItemMeta();
            meta.setDisplayName("§7Next page >>");
            next.setItemMeta(meta);
            bountyList.setItem(53, next);
        }

        ItemStack back = new ItemStack(Material.BARREL);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§6Back to menu");
        back.setItemMeta(backMeta);
        bountyList.setItem(49, back);

        int totalPages = (int) Math.ceil((double) sortedList.size() / bountiesPerPage);

        ItemStack paginator = new ItemStack(Material.COMPASS);
        ItemMeta paginatorMeta = paginator.getItemMeta();
        paginatorMeta.setDisplayName("§7Page " + (page + 1) + "/" + totalPages);
        paginator.setItemMeta(paginatorMeta);
        bountyList.setItem(48, paginator);

        plugin.pages.put(player.getName(), page);
        player.openInventory(bountyList);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.equals("Bounty System") && !title.equals("Bounty List") && !title.equals("Group")) return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null) return;

        if (clicked.getType() == Material.BOOK) {
            plugin.pages.put(player.getName(), 0);
            openBountyList(player);
        }

        if (clicked.getType() == Material.BUNDLE) {
            openGroupGUI(player);
        }

        if (title.equals("Bounty List")) {
            if (clicked.getType() == Material.ARROW) {
                int currentPage = plugin.pages.getOrDefault(player.getName(), 0);
                if (clicked.getItemMeta().getDisplayName().contains("Previous")) {
                    plugin.pages.put(player.getName(), currentPage - 1);
                } else {
                    plugin.pages.put(player.getName(), currentPage + 1);
                }
                openBountyList(player);
            }

            if (clicked.getType() == Material.BARREL) {
                new BountyGUI(plugin).openMenu(player);
            }
        }

        if (clicked.getType() == Material.BARRIER) {
            player.closeInventory();
        }

        if (title.equals("Group")) {
            if (clicked.getType() == Material.EMERALD) {
                player.closeInventory();
                player.performCommand("bounty group create");
            }

            if (clicked.getType() == Material.REDSTONE) {
                plugin.groups.remove(player.getName());
                player.sendMessage("§cYou disbanded the group!");
                player.closeInventory();
            }

            if (clicked.getType() == Material.RED_STAINED_GLASS_PANE) {
                for (BountyGroup group : plugin.groups.values()) {
                    if (group.getMembers().contains(player.getName())) {
                        group.getMembers().remove(player.getName());
                        player.sendMessage("§cYou left the group!");
                        break;
                    }
                }
                player.closeInventory();
            }
        }
    }

    private void openGroupGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "Group");

        BountyGroup foundGroup = null;
        for (BountyGroup group : plugin.groups.values()) {
            if (group.getMembers().contains(player.getName())) {
                foundGroup = group;
                break;
            }
        }

        if (foundGroup == null) {
            ItemStack create = new ItemStack(Material.EMERALD);
            ItemMeta createMeta = create.getItemMeta();
            createMeta.setDisplayName("§aCreate a group");
            create.setItemMeta(createMeta);
            gui.setItem(13, create);
        } else {
            for (String member : foundGroup.getMembers()) {
                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) head.getItemMeta();
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(member));
                meta.setDisplayName("§f" + member);
                head.setItemMeta(meta);
                gui.addItem(head);
            }

            if (foundGroup.getLeader().equals(player.getName())) {
                ItemStack disband = new ItemStack(Material.REDSTONE);
                ItemMeta disbandMeta = disband.getItemMeta();
                disbandMeta.setDisplayName("§cDisband group");
                disband.setItemMeta(disbandMeta);
                gui.setItem(26, disband);
            } else {
                ItemStack leave = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                ItemMeta leaveMeta = leave.getItemMeta();
                leaveMeta.setDisplayName("§cLeave group");
                leave.setItemMeta(leaveMeta);
                gui.setItem(26, leave);
            }
        }

        player.openInventory(gui);
    }
}