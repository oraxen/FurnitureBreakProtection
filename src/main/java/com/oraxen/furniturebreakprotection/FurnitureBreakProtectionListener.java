package com.oraxen.furniturebreakprotection;

import io.th0rgal.oraxen.events.OraxenFurnitureInteractEvent;
import io.th0rgal.oraxen.utils.logs.Logs;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import static com.oraxen.furniturebreakprotection.FurnitureBreakProtection.FURNITURE_KEY;


public class FurnitureBreakProtectionListener implements Listener {

    @EventHandler
    public void onFurniturePop(HangingBreakEvent event) {
        final PersistentDataContainer pdc = event.getEntity().getPersistentDataContainer();
        if (event.getEntity() instanceof ItemFrame && pdc.has(FURNITURE_KEY, PersistentDataType.STRING)) {
            if (event.getCause() == HangingBreakEvent.RemoveCause.ENTITY) return;
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void on(OraxenFurnitureInteractEvent event) {
        Logs.broadcast(event.getFurnitureMechanic().getItemID());
    }
}
