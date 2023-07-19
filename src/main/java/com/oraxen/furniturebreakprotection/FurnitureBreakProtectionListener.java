package com.oraxen.furniturebreakprotection;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.persistence.PersistentDataType;

import static com.oraxen.furniturebreakprotection.FurnitureBreakProtection.FURNITURE_KEY;


public class FurnitureBreakProtectionListener implements Listener {

    @EventHandler
    public void onFurniturePop(HangingBreakEvent event) {
        Entity entity = event.getEntity();
        if (Bukkit.getPluginManager().isPluginEnabled("Oraxen")) return;
        if (entity instanceof ItemFrame && entity.getPersistentDataContainer().has(FURNITURE_KEY, PersistentDataType.STRING)) {
            if (event.getCause() == HangingBreakEvent.RemoveCause.ENTITY) return;
            event.setCancelled(true);
        }
    }
}
