package me.Gabrielhulk10.odisseaBountySystem;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BountyPlaceholder extends PlaceholderExpansion {

    private OdisseaBountySystem plugin;

    public BountyPlaceholder(OdisseaBountySystem plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "bounty";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Gabrielhulk10";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";

        if (params.equals("amount")) {
            if (plugin.bounties.containsKey(player.getName())) {
                return String.valueOf(plugin.bounties.get(player.getName()).getAmount());
            }
            return "0";
        }

        return null;
    }
}