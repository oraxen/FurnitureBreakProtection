package com.oraxen.furniturebreakprotection;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;


public final class FurnitureBreakProtection extends JavaPlugin {
    public static final NamespacedKey FURNITURE_KEY = new NamespacedKey("oraxen", "furniture");
    public static final NamespacedKey ROTATION_KEY = new NamespacedKey("oraxen", "rotation");


    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new FurnitureBreakProtectionListener(), this);
        getCommand("oraxen_fix_furniture").setExecutor(new FurnitureBreakProtectionCommands());

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}

