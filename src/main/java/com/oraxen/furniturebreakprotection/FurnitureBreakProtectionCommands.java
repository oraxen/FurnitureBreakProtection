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
import io.th0rgal.oraxen.shaded.morepersistentdatatypes.DataType;
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
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

import static com.oraxen.furniturebreakprotection.FurnitureBreakProtection.ROTATION_KEY;

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
            if (fix(player, radius)) oraxenAudience.sendMessage(AdventureUtils.MINI_MESSAGE.deserialize("<prefix><green>Fixed furniture in a radius of " + radius + " blocks.", tags));
            else oraxenAudience.sendMessage(AdventureUtils.MINI_MESSAGE.deserialize("<prefix><red>No furniture found in a radius of " + radius + " blocks.", tags));
        } else {
            if (oraxenEnabled) {
                TagResolver tags = TagResolver.resolver(TagResolver.standard(), GlyphTag.RESOLVER, ShiftTag.RESOLVER, AdventureUtils.tagResolver("prefix", Message.PREFIX.toString()));
                OraxenPlugin.get().getAudience().sender(sender).sendMessage(AdventureUtils.MINI_MESSAGE.deserialize("<prefix><red>You must be an OP player to use this command!", tags));
            }
            else sender.sendMessage("Oraxen is not enabled!");
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

            float orientation = pdc.getOrDefault(FurnitureMechanic.ORIENTATION_KEY, PersistentDataType.FLOAT, 0f);
            final BlockLocation blockLocation = new BlockLocation(Objects.requireNonNull(pdc.get(FurnitureMechanic.ROOT_KEY, PersistentDataType.STRING)));
            final Rotation rotation = pdc.getOrDefault(ROTATION_KEY, DataType.asEnum(Rotation.class), mechanic.hasRotation() ? mechanic.getRotation()
                    : getRotation(orientation, mechanic.hasBarriers() && mechanic.getBarriers().size() > 1));
            mechanic.removeSolid(block.getWorld(), blockLocation, orientation);
            new CustomBlockData(block, OraxenPlugin.get()).clear();
            mechanic.place(rotation, orientation, mechanic.getFacing(), blockLocation.toLocation(player.getWorld()), player);
            return true;
        } else return false;
    }

    private Rotation getRotation(final double yaw, final boolean restricted) {
        int id = (int) (((Location.normalizeYaw((float) yaw) + 180) * 8 / 360) + 0.5) % 8;
        if (restricted && id % 2 != 0)
            id -= 1;
        return Rotation.values()[id];
    }
}
