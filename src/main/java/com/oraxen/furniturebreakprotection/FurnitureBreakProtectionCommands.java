package com.oraxen.furniturebreakprotection;

import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.mechanics.provided.gameplay.furniture.BlockLocation;
import io.th0rgal.oraxen.mechanics.provided.gameplay.furniture.FurnitureFactory;
import io.th0rgal.oraxen.mechanics.provided.gameplay.furniture.FurnitureListener;
import io.th0rgal.oraxen.mechanics.provided.gameplay.furniture.FurnitureMechanic;
import io.th0rgal.oraxen.shaded.customblockdata.CustomBlockData;
import io.th0rgal.oraxen.utils.BlockHelpers;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public class FurnitureBreakProtectionCommands implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean oraxenEnabled = Bukkit.getPluginManager().isPluginEnabled("Oraxen");
        if (sender instanceof Player player && player.isOp() && oraxenEnabled) {
            int radius = 20;
            if (args.length != 0) {
                try {
                    radius = Integer.parseInt(args[0]);
                    if (radius < 1) {
                        player.sendMessage("Radius must be greater than 0!");
                        return true;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
            if (fix(player, radius))
                player.sendMessage("Fixed furniture in a radius of " + radius + " blocks.");
            else player.sendMessage("No furniture found in a radius of " + radius + " blocks.");
        } else {
            if (oraxenEnabled)
                sender.sendMessage("You must be an OP player to use this command!");
            else
                sender.sendMessage("Oraxen is not enabled!");
        }
        return true;
    }

    private boolean fix(Player player, int radius) {
        Location loc = player.getLocation();
        int fixed = 0;
        for (int i = 0; i < radius; i++) {
            for (int j = 0; j < radius; j++) {
                if (loc.getBlock().getType() == Material.BARRIER) {
                    FurnitureMechanic mechanic = FurnitureListener.getFurnitureMechanic(loc.getBlock());
                    if (mechanic == null) {
                        loc.getBlock().setType(Material.AIR, false);
                        new CustomBlockData(loc.getBlock(), OraxenPlugin.get()).clear();
                        continue;
                    }

                    PersistentDataContainer pdc = BlockHelpers.getPDC(loc.getBlock());
                    String id = pdc.get(FurnitureMechanic.FURNITURE_KEY, PersistentDataType.STRING);
                    mechanic = (FurnitureMechanic) FurnitureFactory.getInstance().getMechanic(id);
                    if (mechanic == null) {
                        loc.getBlock().setType(Material.AIR, false);
                        new CustomBlockData(loc.getBlock(), OraxenPlugin.get()).clear();
                        continue;
                    }

                    float orientation = pdc.getOrDefault(FurnitureMechanic.ORIENTATION_KEY, PersistentDataType.FLOAT, 0f);
                    final BlockLocation blockLocation = new BlockLocation(Objects.requireNonNull(pdc.get(FurnitureMechanic.ROOT_KEY, PersistentDataType.STRING)));
                    final Rotation rotation = mechanic.hasRotation() ? mechanic.getRotation()
                            : getRotation(player.getEyeLocation().getYaw(), mechanic.hasBarriers() && mechanic.getBarriers().size() > 1);

                    loc.getBlock().setType(Material.AIR, false);
                    new CustomBlockData(loc.getBlock(), OraxenPlugin.get()).clear();
                    mechanic.place(rotation, orientation, mechanic.getFacing(), blockLocation.toLocation(player.getWorld()), player);
                    fixed++;
                }
                loc = loc.subtract(0, 0, 1);
            }
            loc = loc.add(-1, 0, 9);
        }
        return fixed > 0;
    }

    private Rotation getRotation(final double yaw, final boolean restricted) {
        int id = (int) (((Location.normalizeYaw((float) yaw) + 180) * 8 / 360) + 0.5) % 8;
        if (restricted && id % 2 != 0)
            id -= 1;
        return Rotation.values()[id];
    }
}
