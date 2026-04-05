package me.Gabrielhulk10.odisseaBountySystem;

import java.util.HashMap;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class OdisseaBountySystem extends JavaPlugin {

    public HashMap<String, Bounty> bounties = new HashMap<>();
    public HashMap<String, BountyGroup> groups = new HashMap<>();
    public HashMap<String, Integer> pages = new HashMap<>();
    public Economy economy;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getLogger().info("BountyPlugin started!");
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().severe("Vault not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        economy = rsp.getProvider();
        getCommand("bounty").setExecutor(new BountyCommand(this));
        getCommand("bounty").setTabCompleter(new BountyCommand(this));
        getServer().getPluginManager().registerEvents(new BountyListener(this), this);
        if (getConfig().contains("bounties")) {
            for (String player : getConfig().getConfigurationSection("bounties").getKeys(false)) {
                int amount = getConfig().getInt("bounties." + player);
                Bounty bounty = new Bounty("unknown", player, amount, null);
                bounties.put(player, bounty);
            }
        }
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new BountyPlaceholder(this).register();
            getLogger().info("PlaceholderAPI found and BountyPlaceholder registered!");
        } else {
            getLogger().warning("PlaceholderAPI not found! Placeholders won't work.");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("BountyPlugin disabled.");
    }
}