package org.simpleEventManager.manager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.simpleEventManager.SimpleEventManager;

import java.util.List;

public class RewardGUI implements Listener {

    private final SimpleEventManager plugin;
    private final Player player;
    private final Inventory inventory;

    private boolean hasChosen = false;

    public RewardGUI(SimpleEventManager plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 3 * 9, "Choisis ta récompense !");
        Bukkit.getPluginManager().registerEvents(this, plugin);
        open();
    }

    private void open() {
        FileConfiguration config = plugin.getConfig(); // ou getRewardConfig()
        ConfigurationSection section = config.getConfigurationSection("rewards.1");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            ConfigurationSection rewardSection = section.getConfigurationSection(key);
            if (rewardSection == null) continue;

            String display = rewardSection.getString("displayname", "Récompense");
            String lore = rewardSection.getString("lore", "");
            String command = rewardSection.getString("reward");

            ItemStack item = new ItemStack(Material.CHEST);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(display.replace("&", "§"));
            if (!lore.isEmpty()) {
                meta.setLore(List.of(lore));
            }
            meta.getPersistentDataContainer().set(
                    new org.bukkit.NamespacedKey(plugin, "reward_command"),
                    org.bukkit.persistence.PersistentDataType.STRING,
                    command
            );
            item.setItemMeta(meta);
            inventory.addItem(item);
        }

        player.openInventory(inventory);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getWhoClicked().equals(player)) return;
        if (!e.getInventory().equals(inventory)) return;

        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String command = clicked.getItemMeta().getPersistentDataContainer().get(
                new org.bukkit.NamespacedKey(plugin, "reward_command"),
                org.bukkit.persistence.PersistentDataType.STRING
        );
        if (command != null) {
            command = command.replace("%player%", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            hasChosen = true;
            player.closeInventory();
            InventoryClickEvent.getHandlerList().unregister(this);
            InventoryCloseEvent.getHandlerList().unregister(this);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!e.getPlayer().equals(player)) return;
        if (!hasChosen) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> player.openInventory(inventory), 2L);
        }
    }
}
