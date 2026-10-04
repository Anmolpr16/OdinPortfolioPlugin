package io.github.anmolgoraya.odinportfolioplugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class OdinPortfolioPlugin extends JavaPlugin implements Listener {

    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<UUID, Long> spawnCooldowns = new HashMap<>();
    private Location odinSpawn;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSpawn();
        Objects.requireNonNull(getCommand("odin")).setExecutor(new OdinCommand(this));
        Objects.requireNonNull(getCommand("odin")).setTabCompleter(new OdinTabCompleter());
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("OdinPortfolioPlugin v2.0.0 enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("OdinPortfolioPlugin disabled.");
    }

    void loadSpawn() {
        if (getConfig().contains("spawn.world")) {
            var world = Bukkit.getWorld(getConfig().getString("spawn.world", ""));
            if (world != null) {
                odinSpawn = new Location(
                        world,
                        getConfig().getDouble("spawn.x"),
                        getConfig().getDouble("spawn.y"),
                        getConfig().getDouble("spawn.z"),
                        (float) getConfig().getDouble("spawn.yaw"),
                        (float) getConfig().getDouble("spawn.pitch")
                );
            }
        }
    }

    void setSpawn(Location location) {
        odinSpawn = location.clone();
        getConfig().set("spawn.world", location.getWorld().getName());
        getConfig().set("spawn.x", location.getX());
        getConfig().set("spawn.y", location.getY());
        getConfig().set("spawn.z", location.getZ());
        getConfig().set("spawn.yaw", location.getYaw());
        getConfig().set("spawn.pitch", location.getPitch());
        saveConfig();
    }

    Location getOdinSpawn() {
        return odinSpawn;
    }

    Component msg(String text) {
        return mm.deserialize(getConfig().getString("messages.prefix", "") + text);
    }

    Component raw(String text) {
        return mm.deserialize(text);
    }

    boolean onCooldown(Player player) {
        long now = System.currentTimeMillis();
        long until = spawnCooldowns.getOrDefault(player.getUniqueId(), 0L);
        if (until <= now) return false;

        long seconds = Math.max(1, (until - now + 999) / 1000);
        player.sendMessage(msg(getConfig().getString("messages.cooldown", "<red>Cooldown: %seconds%s")
                .replace("%seconds%", String.valueOf(seconds))));
        return true;
    }

    void startCooldown(Player player) {
        int seconds = getConfig().getInt("cooldowns.spawn-seconds", 10);
        spawnCooldowns.put(player.getUniqueId(), System.currentTimeMillis() + seconds * 1000L);
    }

    void openMenu(Player player) {
        int size = Math.max(9, Math.min(54, getConfig().getInt("menu.size", 27)));
        Inventory inv = Bukkit.createInventory(null, size, raw(getConfig().getString("menu.title", "<dark_gray>Odin")));
        addConfiguredItem(inv, "profile");
        addConfiguredItem(inv, "spawn");
        addConfiguredItem(inv, "stats");
        player.openInventory(inv);
    }

    private void addConfiguredItem(Inventory inv, String key) {
        ConfigurationSection section = getConfig().getConfigurationSection("menu.items." + key);
        if (section == null) return;
        int slot = section.getInt("slot", 0);
        Material material = Material.matchMaterial(section.getString("material", "STONE"));
        if (material == null || slot < 0 || slot >= inv.getSize()) return;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(raw(section.getString("name", "<white>" + key)));
        List<Component> lore = section.getStringList("lore").stream().map(this::raw).toList();
        meta.lore(lore);
        item.setItemMeta(meta);
        inv.setItem(slot, item);
    }

    void showStats(Player player) {
        long playTicks = player.getStatistic(org.bukkit.Statistic.PLAY_ONE_MINUTE);
        long minutes = playTicks / (20L * 60L);
        player.sendMessage(msg("<white>Player: <yellow>" + player.getName()));
        player.sendMessage(msg("<white>Play time: <yellow>" + minutes + " minutes"));
        player.sendMessage(msg("<white>Health: <yellow>" + String.format("%.1f", player.getHealth())));
        player.sendMessage(msg("<white>World: <yellow>" + player.getWorld().getName()));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        p.sendMessage(msg(getConfig().getString("messages.welcome", "<gold>Welcome, %player%")
                .replace("%player%", p.getName())));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        spawnCooldowns.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().title().equals(raw(getConfig().getString("menu.title", "<dark_gray>Odin")))) return;

        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == getConfig().getInt("menu.items.profile.slot", 11)) {
            showStats(player);
        } else if (slot == getConfig().getInt("menu.items.stats.slot", 15)) {
            showStats(player);
        } else if (slot == getConfig().getInt("menu.items.spawn.slot", 13)) {
            player.closeInventory();
            if (odinSpawn == null) {
                player.sendMessage(msg("<red>Odin spawn has not been configured yet."));
            } else if (!onCooldown(player)) {
                player.teleportAsync(odinSpawn);
                startCooldown(player);
                player.sendMessage(msg("<green>Teleported to Odin spawn."));
            }
        }
    }
}
