package com.oraxen.furniturebreakprotection;

import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.api.OraxenFurniture;
import io.th0rgal.oraxen.config.Message;
import io.th0rgal.oraxen.font.GlyphTag;
import io.th0rgal.oraxen.font.ShiftTag;
import io.th0rgal.oraxen.mechanics.provided.gameplay.furniture.BlockLocation;
import io.th0rgal.oraxen.mechanics.provided.gameplay.furniture.FurnitureFactory;
import io.th0rgal.oraxen.mechanics.provided.gameplay.furniture.FurnitureMechanic;
import io.th0rgal.oraxen.shaded.customblockdata.CustomBlockData;
import io.th0rgal.oraxen.shaded.kyori.adventure.audience.Audience;
import io.th0rgal.oraxen.shaded.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import io.th0rgal.oraxen.utils.AdventureUtils;
import io.th0rgal.oraxen.utils.BlockHelpers;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public class FurnitureBreakProtectionCommands implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean oraxenEnabled = Bukkit.getPluginManager().isPluginEnabled("Oraxen");
        if (sender instanceof Player player && player.isOp() && oraxenEnabled) {
            Audience oraxenAudience = OraxenPlugin.get().getAudience().player(player);
            TagResolver tags = TagResolver.resolver(TagResolver.standard(), GlyphTag.RESOLVER, ShiftTag.RESOLVER, AdventureUtils.tagResolver("prefix", Message.PREFIX.toString()));
            double radius = 20;
            if (args.length != 0) {
                try {
                    radius = Double.parseDouble(args[0].replace(",", "."));
                    if (radius < 1) {
                        oraxenAudience.sendMessage(AdventureUtils.MINI_MESSAGE.deserialize("<prefix><red>Radius must be greater than 0!", tags));
                        return true;
                    }
                } catch (NumberFormatException ignored) {
                    oraxenAudience.sendMessage(AdventureUtils.MINI_MESSAGE.deserialize("<prefix><red>Radius must be a valid number, " + args[0].replace(",", ".") + " is not.", tags));
                    return true;
                }
            }
            if (fix(player, radius))
                oraxenAudience.sendMessage(AdventureUtils.MINI_MESSAGE.deserialize("<prefix><green>Fixed furniture in a radius of " + radius + " blocks.", tags));
            else
                oraxenAudience.sendMessage(AdventureUtils.MINI_MESSAGE.deserialize("<prefix><red>No furniture found in a radius of " + radius + " blocks.", tags));
        } else {
            if (oraxenEnabled) {
                TagResolver tags = TagResolver.resolver(TagResolver.standard(), GlyphTag.RESOLVER, ShiftTag.RESOLVER, AdventureUtils.tagResolver("prefix", Message.PREFIX.toString()));
                OraxenPlugin.get().getAudience().sender(sender).sendMessage(AdventureUtils.MINI_MESSAGE.deserialize("<prefix><red>You must be an OP player to use this command!", tags));
            } else sender.sendMessage("Oraxen is not enabled!");
        }
        return true;
    }

    private boolean fix(Player player, double radius) {
        Location loc = player.getLocation();
        int fixed = 0;
        for (double x = loc.getX() - radius; x < loc.getX() + radius; x++)
            for (double y = loc.getY() - radius; y < loc.getY() + radius; y++)
                for (double z = loc.getZ() - radius; z < loc.getZ() + radius; z++)
                    if (replace(new Location(player.getWorld(), x, y, z).getBlock(), player)) fixed++;

        return fixed != 0;
    }

    private boolean replace(Block block, Player player) {
        if (block.getType() == Material.BARRIER) {
            FurnitureMechanic mechanic = OraxenFurniture.getFurnitureMechanic(block);
            if (mechanic == null) {
                block.setType(Material.AIR, false);
                new CustomBlockData(block, OraxenPlugin.get()).clear();
                return false;
            }

            PersistentDataContainer pdc = BlockHelpers.getPDC(block);
            String id = pdc.get(FurnitureMechanic.FURNITURE_KEY, PersistentDataType.STRING);
            mechanic = (FurnitureMechanic) FurnitureFactory.getInstance().getMechanic(id);
            if (mechanic == null) {
                block.setType(Material.AIR, false);
                new CustomBlockData(block, OraxenPlugin.get()).clear();
                return false;
            }

            Entity baseEntity = mechanic.getBaseEntity(block);
            float yaw = FurnitureMechanic.getFurnitureYaw(baseEntity);
            final BlockLocation blockLocation = new BlockLocation(Objects.requireNonNull(pdc.get(FurnitureMechanic.ROOT_KEY, PersistentDataType.STRING)));
            final Rotation rotation = FurnitureMechanic.yawToRotation(yaw);
            OraxenFurniture.remove(baseEntity, null);
            new CustomBlockData(block, OraxenPlugin.get()).clear();
            OraxenFurniture.place(blockLocation.toLocation(player.getWorld()), mechanic.getItemID(), rotation, baseEntity.getFacing());
            return true;
        } else {
            for (final Entity entity : block.getWorld().getNearbyEntities(block.getLocation(), 1,1,1)) {
                if (!OraxenFurniture.isFurniture(entity)) continue;
                if (entity.getType() == EntityType.INTERACTION) continue;
                FurnitureMechanic mechanic = OraxenFurniture.getFurnitureMechanic(entity);
                if (!OraxenFurniture.remove(entity, null)) continue;
                new CustomBlockData(block, OraxenPlugin.get()).clear();
                OraxenFurniture.place(entity.getLocation(), mechanic.getItemID(), FurnitureMechanic.yawToRotation(entity.getLocation().getYaw()), entity.getFacing());
            }
            return false;
        }
    }
}
