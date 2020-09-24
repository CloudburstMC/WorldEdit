/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.cloudburst;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.nbt.NbtMap;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.NotABlockException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.internal.block.BlockStateIdAccess;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.registry.state.BooleanProperty;
import com.sk89q.worldedit.registry.state.EnumProperty;
import com.sk89q.worldedit.registry.state.IntegerProperty;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import org.cloudburstmc.server.Server;
import org.cloudburstmc.server.block.trait.BlockTrait;
import org.cloudburstmc.server.block.trait.BooleanBlockTrait;
import org.cloudburstmc.server.block.trait.EnumBlockTrait;
import org.cloudburstmc.server.block.trait.IntegerBlockTrait;
import org.cloudburstmc.server.item.behavior.Item;
import org.cloudburstmc.server.level.Level;
import org.cloudburstmc.server.level.biome.Biome;
import org.cloudburstmc.server.registry.BiomeRegistry;
import org.cloudburstmc.server.registry.BlockRegistry;
import org.cloudburstmc.server.utils.Identifier;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

public class CloudburstAdapter {

    private CloudburstAdapter() {
    }

    public static Biome adapt(BiomeType biomeType) {
        Identifier biomeIdentifier = Identifier.fromString(biomeType.getId());
        return BiomeRegistry.get().getBiome(biomeIdentifier);
    }

    public static Direction adapt(org.cloudburstmc.server.math.Direction direction) {
        if (direction == null) {
            return null;
        }

        switch (direction) {
            case NORTH:
                return Direction.NORTH;
            case SOUTH:
                return Direction.SOUTH;
            case WEST:
                return Direction.WEST;
            case EAST:
                return Direction.EAST;
            case DOWN:
                return Direction.DOWN;
            case UP:
            default:
                return Direction.UP;
        }
    }

    private static final ParserContext TO_BLOCK_CONTEXT = new ParserContext();

    static {
        TO_BLOCK_CONTEXT.setRestricted(false);
    }

    private static final BiMap<BlockState, org.cloudburstmc.server.block.BlockState> blockStateCache = HashBiMap.create();

    public static BlockState adapt(org.cloudburstmc.server.block.BlockState blockState) {
        return BlockStateIdAccess.getBlockStateById(BlockRegistry.get().getRuntimeId(blockState));
    }

    public static BlockState asBlockState(Item item) throws WorldEditException {
        checkNotNull(item);
        if (item.getBlock() != null) {
            return adapt(item.getBlock());
        } else {
            throw new NotABlockException();
        }
    }

    public static CompoundTag adapt(NbtMap nbtMap) {
        return new CompoundTag(Collections.emptyMap());
    }

    public static NbtMap adapt(CompoundTag compoundTag) {
        return NbtMap.EMPTY;
    }

    public static org.cloudburstmc.server.level.Location adapt(Location location) {
        checkNotNull(location);
        Vector3 position = location.toVector();
        return org.cloudburstmc.server.level.Location.from(Vector3f.from(position.getX(), position.getY(), position.getZ()),
                location.getYaw(), location.getPitch(), adapt((World) location.getExtent()));
    }

    public static org.cloudburstmc.server.block.BlockState adapt(BlockType blockType) {
        return BlockRegistry.get().getBlock(Identifier.fromString(blockType.getId()));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static org.cloudburstmc.server.block.BlockState adapt(BlockState blockState) {
        org.cloudburstmc.server.block.BlockState state = adapt(blockState.getBlockType());

        for (Map.Entry<? extends Property<?>, Object> entry : blockState.getStates().entrySet()) {
            BlockTrait<? extends Comparable<?>> trait = adapt(entry.getKey());

            if (trait instanceof EnumBlockTrait) {
                state = state.withTrait((EnumBlockTrait) trait, (Enum) trait.parseValue((String) entry.getValue()));
            } else if (trait instanceof BooleanBlockTrait) {
                state = state.withTrait((BooleanBlockTrait) trait, (boolean) entry.getValue());
            } else if (trait instanceof IntegerBlockTrait) {
                state = state.withTrait((IntegerBlockTrait) trait, (int) entry.getValue());
            } else {
                throw new IllegalStateException("Unable to find trait for " + entry.getKey());
            }
        }

        blockStateCache.put(blockState, state);

        return state;
    }

    private static final Map<Property<?>, BlockTrait<?>> propertyCache = new IdentityHashMap<>();

    public static BlockTrait<? extends Comparable<?>> adapt(Property<?> property) {
        return propertyCache.get(property);
    }

    public static Property<?> adapt(BlockTrait<?> trait) {
        Property<?> property;

        if (trait instanceof EnumBlockTrait) {
            property = new EnumProperty(trait.getName(), ((EnumBlockTrait<? extends Enum<?>>) trait).getPossibleValues()
                    .stream()
                    .map(Enum::name)
                    .map(String::toLowerCase)
                    .collect(Collectors.toList()));
        } else if (trait instanceof IntegerBlockTrait) {
            property = new IntegerProperty(trait.getName(), ((IntegerBlockTrait) trait).getPossibleValues());
        } else if (trait instanceof BooleanBlockTrait) {
            property = new BooleanProperty(trait.getName(), ((BooleanBlockTrait) trait).getPossibleValues());
        } else {
            throw new IllegalArgumentException("Unknown trait type " + trait);
        }

        propertyCache.put(property, trait);

        return property;
    }

    public static Location adapt(org.cloudburstmc.server.level.Location location) {
        checkNotNull(location);
        Vector3 position = asVector(location);
        return new Location(adapt(location.getLevel()),
                position,
                location.getYaw(),
                location.getPitch());
    }

    private static Vector3 asVector(org.cloudburstmc.server.level.Location location) {
        return Vector3.at(location.getX(), location.getY(), location.getZ());
    }

    public static Level adapt(World world) {
        checkNotNull(world);
        if (world instanceof CloudburstWorld) {
            return ((CloudburstWorld) world).getWorld();
        } else {
            Level match = Server.getInstance().getLevel(world.getName());
            if (match != null) {
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
        String identifier = item.getId().toString().toLowerCase(Locale.US);
        ItemType type = ItemTypes.get(identifier);
        return new BaseItemStack(type, CloudburstAdapter.adapt(item.getTag()), item.getCount());
    }

    public static Item adapt(BaseItemStack itemStack) {
        return Item.get(Identifier.fromString(itemStack.getType().getId()), 0, itemStack.getAmount(), CloudburstAdapter.adapt(itemStack.getNbtData()));
    }
}
