package me.Gabrielhulk10.odisseaBountySystem;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BountyGUI {

    private final OdisseaBountySystem plugin;

    public BountyGUI(OdisseaBountySystem plugin) {
        this.plugin = plugin;
    }

    public void openMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 27, "Bounty System");
        player.openInventory(menu);

        ItemStack list = new ItemStack(Material.BOOK);
        ItemMeta listMeta = list.getItemMeta();
        listMeta.setDisplayName("Bounty List");
        list.setItemMeta(listMeta);
        menu.setItem(11, list);

        ItemStack group = new ItemStack(Material.BUNDLE);
        ItemMeta groupMeta = group.getItemMeta();
        groupMeta.setDisplayName("Group");
        group.setItemMeta(groupMeta);
        menu.setItem(13, group);

        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName("Close");
        close.setItemMeta(closeMeta);
        menu.setItem(15, close);
    }
}