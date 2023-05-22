package com.oraxen.furniturebreakprotection;

import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.api.OraxenFurniture;
import io.th0rgal.oraxen.config.Message;
import io.th0rgal.oraxen.font.GlyphTag;
import io.th0rgal.oraxen.font.ShiftTag;
import io.th0rgal.oraxen.mechanics.provided.gameplay.furniture.FurnitureMechanic;
import io.th0rgal.oraxen.shaded.kyori.adventure.audience.Audience;
import io.th0rgal.oraxen.shaded.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import io.th0rgal.oraxen.utils.AdventureUtils;
import io.th0rgal.oraxen.utils.logs.Logs;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;

import java.util.function.Predicate;

public class FurnitureBreakProtectionCommands implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean oraxenEnabled = Bukkit.getPluginManager().isPluginEnabled("Oraxen");
        if (sender instanceof Player player && player.isOp() && oraxenEnabled) {
            Audience oraxenAudience = OraxenPlugin.get().getAudience().player(player);
            TagResolver tags = TagResolver.resolver(TagResolver.standard(), GlyphTag.RESOLVER, ShiftTag.RESOLVER, AdventureUtils.tagResolver("prefix", Message.PREFIX.toString()));
            int radius = 20;
            if (args.length != 0) {
                try {
                    radius = Integer.parseInt(args[0]);
                    if (radius < 1) {
                        oraxenAudience.sendMessage(AdventureUtils.MINI_MESSAGE.deserialize("<prefix><red>Radius must be greater than 0!", tags));
                        return true;
                    }
                } catch (NumberFormatException ignored) {
                    oraxenAudience.sendMessage(AdventureUtils.MINI_MESSAGE.deserialize("<prefix><red>Radius must be a valid number, " + args[0].replace(",", ".") + " is not.", tags));
                    return true;
                }
            }
            if (replace(player, radius))
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

    private boolean replace(Player player, int radius) {
        int fixed = 0;
        Predicate<Entity> predicate = OraxenPlugin.supportsDisplayEntities ? Predicate.not(e -> e instanceof Player || e instanceof Interaction) : Predicate.not(e -> e instanceof Player);
        for (final Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius, predicate)) {
            Logs.debug(entity.getUniqueId());
            Logs.debug(entity.getType());
            Logs.debug(entity.getLocation());
            if (!OraxenFurniture.isFurniture(entity)) continue;
            if (entity.getType() == EntityType.INTERACTION) continue;
            FurnitureMechanic mechanic = OraxenFurniture.getFurnitureMechanic(entity);
            final float yaw = FurnitureMechanic.getFurnitureYaw(entity);
            Logs.debug("yaw: " + yaw);
            if (!OraxenFurniture.remove(entity, null)) continue;
            Logs.debug("yaw2: " + yaw);
            Entity placedEntity = mechanic.place(entity.getLocation(), yaw, entity.getFacing());
            if (placedEntity instanceof ItemFrame itemFrame) {
                itemFrame.setRotation(FurnitureMechanic.yawToRotation(yaw));
            } else {
                placedEntity.setRotation(yaw, 0);
            }
            fixed++;
            Logs.debug("yaw3: " + yaw);
        }
        return fixed > 0;
    }
}
