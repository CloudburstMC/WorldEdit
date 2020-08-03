package com.sk89q.worldedit.cloudburst;

import cn.nukkit.Server;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.biome.Biome;
import cn.nukkit.level.biome.BiomeBuilder;
import cn.nukkit.level.biome.BiomeIds;
import cn.nukkit.registry.BiomeRegistry;
import cn.nukkit.registry.ItemRegistry;
import cn.nukkit.utils.Identifier;
import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.nbt.NbtMap;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.item.ItemType;

import static com.google.common.base.Preconditions.checkNotNull;

public class CloudburstAdapter {

    private CloudburstAdapter() {
    }

    public static Biome adapt(BiomeType biomeType) {
        Identifier biomeIdentifier = Identifier.fromString(biomeType.getId());
        return BiomeRegistry.get().getBiome(biomeIdentifier);
    }

    public static Tag toNative(NbtMap nbtMap) {

    }

    public static cn.nukkit.level.Location adapt(Location location) {
        checkNotNull(location);
        Vector3 position = location.toVector();
        return cn.nukkit.level.Location.from(Vector3f.from(position.getX(), position.getY(), position.getZ()),
                location.getYaw(), location.getPitch(), adapt(location.getExtent()));
    }

    public static Location adapt(cn.nukkit.level.Location location) {
        checkNotNull(location);
        Vector3 position = asVector(location);
        return new Location(adapt(location.getLevel()),
                position,
                location.getYaw(),
                location.getPitch());
    }

    private static Vector3 asVector(cn.nukkit.level.Location location) {
        return Vector3.at(location.getX(), location.getY(), location.getZ());
    }

    public static Level adapt(World world) {
        checkNotNull(world);
        if(world instanceof CloudburstWorld) {
            return ((Level) world).getWorld();
        } else {
            Level match = Server.getInstance().getLevel(world.getName());
            if(match != null) {
                return match;
            } else {
                throw new IllegalArgumentException("Can't find a Cloudburst world for " + world.getName());
            }
        }
    }

    public static World adapt(Level level) {
        checkNotNull(level);
        return new CloudburstWorld(level);
    }

    public static BaseItemStack adapt(Item item) {
        return new BaseItemStack(ItemType.REGISTRY.get(item.getId().toString()), CloudburstAdapter.adapt(item.getTag()), item.getCount());
    }

    public static Item adapt(BaseItemStack itemStack) {
        return Item.get(Identifier.fromString(itemStack.getType().getId()), 0, itemStack.getAmount(), CloudburstAdapter.adapt(itemStack.getNbtData()));
    }
}
