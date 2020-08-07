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

import com.google.common.collect.ImmutableSet;
import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.math.vector.Vector3i;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.world.AbstractWorld;
import com.sk89q.worldedit.world.WorldUnloadedException;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import org.cloudburstmc.server.Server;
import org.cloudburstmc.server.block.Block;
import org.cloudburstmc.server.block.BlockPalette;
import org.cloudburstmc.server.block.BlockState;
import org.cloudburstmc.server.block.BlockStates;
import org.cloudburstmc.server.blockentity.Chest;
import org.cloudburstmc.server.inventory.DoubleChestInventory;
import org.cloudburstmc.server.inventory.Inventory;
import org.cloudburstmc.server.inventory.InventoryHolder;
import org.cloudburstmc.server.level.Level;
import org.cloudburstmc.server.utils.Identifier;

import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class CloudburstWorld extends AbstractWorld {

    private final WeakReference<Level> worldRef;

    protected CloudburstWorld(Level level) {
        checkNotNull(level);
        this.worldRef = new WeakReference<>(level);
    }

    public Level getWorldChecked() throws WorldEditException {
        Level world = worldRef.get();
        if (world != null) {
            return world;
        } else {
            throw new WorldUnloadedException();
        }
    }

    public Level getWorld() {
        Level world = worldRef.get();
        if (world != null) {
            return world;
        } else {
            throw new RuntimeException("The reference to the world was lost (i.e. the world may have been unloaded)");
        }
    }

    @Override
    public String getName() {
        return getWorld().getName();
    }

    @Override
    public String getId() {
        return getWorld().getId();
    }

    @Override
    public Path getStoragePath() {
        return Paths.get(Server.getInstance().getDataPath()).resolve("worlds").resolve(getId());
    }

    @Override
    public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block, SideEffectSet sideEffects) throws WorldEditException {
        checkNotNull(position);
        checkNotNull(block);

        Level world = getWorldChecked();

        Vector3i pos = Vector3i.from(position.getBlockX(), position.getBlockY(), position.getBlockZ());
        BlockState blockState = getBlockState(block);
        System.out.println("SET BLOCK IS BEING CALLED! WOEHOE " + pos + " - " + blockState.getType());
        System.out.println("OLD BLOCK " + world.getBlock(pos).getState().getType());
        world.getBlock(pos).set(blockState);
        System.out.println("NEW BLOCK " + world.getBlock(pos).getState().getType());
        return false;
    }

    protected BlockState getBlockState(BlockStateHolder<?> block) {
        if (block instanceof com.sk89q.worldedit.world.block.BlockState) {
            BlockState state = BlockPalette.INSTANCE.getDefaultState(Identifier.fromString(block.getBlockType().getId()));
            return state != null ? state : BlockStates.AIR;
        } else if (block instanceof BaseBlock) {
            BlockState state = BlockPalette.INSTANCE.getDefaultState(Identifier.fromString(block.getBlockType().getId()));
            return state != null ? state : BlockStates.AIR;
        } else {
            throw new UnsupportedOperationException("Missing Cloudburst adapter for WorldEdit!");
        }
    }

    @Override
    public Set<SideEffect> applySideEffects(BlockVector3 position, com.sk89q.worldedit.world.block.BlockState previousType, SideEffectSet sideEffectSet) throws WorldEditException {
        return ImmutableSet.of();
    }

    @Override
    public int getBlockLightLevel(BlockVector3 position) {
        checkNotNull(position);
        return getWorld().getBlockLightAt(position.getBlockX(), position.getBlockY(), position.getBlockZ());
    }

    @Override
    public boolean clearContainerBlockContents(BlockVector3 position) {
        checkNotNull(position);

        Block block = getWorld().getBlock(Vector3i.from(position.getBlockX(), position.getBlockY(), position.getBlockZ()));
        BlockState blockState = block.getState();

        if (!(blockState instanceof InventoryHolder)) {
            return false;
        }

        InventoryHolder inventoryHolder = (InventoryHolder) blockState;
        Inventory inventory = inventoryHolder.getInventory();
        if (inventoryHolder instanceof Chest) {
            inventory = getBlockInventory((Chest) inventoryHolder);
        }

        inventory.clearAll();
        return true;
    }

    private Inventory getBlockInventory(Chest chest) {
        if (chest.getInventory() instanceof DoubleChestInventory) {
            DoubleChestInventory inventory = (DoubleChestInventory) chest.getInventory();
            if (inventory.getLeftSide().getHolder().equals(chest)) {
                return inventory.getLeftSide();
            } else if (inventory.getRightSide().getHolder().equals(chest)) {
                return inventory.getRightSide();
            } else {
                return inventory;
            }
        } else {
            return chest.getInventory();
        }
    }

    @Override
    public void dropItem(Vector3 position, BaseItemStack item) {
        Level world = getWorld();
        world.dropItem(Vector3f.from(position.getX(), position.getY(), position.getZ()), CloudburstAdapter.adapt(item));
    }

    @Override
    public void simulateBlockMine(BlockVector3 position) {
        //TODO implement with the correct usage when the api is done
        getWorld().getBlock(position.getBlockX(), position.getBlockY(), position.getBlockZ()).set(BlockStates.AIR);
    }

    @Override
    public boolean generateTree(TreeGenerator.TreeType type, EditSession editSession, BlockVector3 position) throws MaxChangedBlocksException {
        //TODO, not implemented yet
        return false;
    }

    @Override
    public BlockVector3 getSpawnPosition() {
        Level world = getWorld();
        return CloudburstAdapter.adapt(world.getSpawnLocation()).toVector().toBlockPoint();
    }

    @Override
    public List<? extends Entity> getEntities(Region region) {
        List<Entity> entities = new ArrayList<>();
        for (org.cloudburstmc.server.entity.Entity entity : getWorld().getEntities()) {
            org.cloudburstmc.server.level.Location location = entity.getLocation();
            if (region.contains(BlockVector3.at(location.getX(), location.getY(), location.getZ()))) {
                entities.add(new CloudburstEntity(entity));
            }
        }
        return entities;
    }

    @Override
    public List<? extends Entity> getEntities() {
        List<Entity> entities = new ArrayList<>();
        for (org.cloudburstmc.server.entity.Entity entity : getWorld().getEntities()) {
            entities.add(new CloudburstEntity(entity));
        }
        return entities;
    }

    //TODO
    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        return null;
    }

    @Override
    public com.sk89q.worldedit.world.block.BlockState getBlock(BlockVector3 position) {
        return CloudburstAdapter.adapt(getWorld().getBlockAt(position.getBlockX(), position.getBlockY(), position.getBlockZ()));
    }

    @Override
    public BaseBlock getFullBlock(BlockVector3 position) {
        return getBlock(position).toBaseBlock();
    }

    @Override
    public int hashCode() {
        return getWorld().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        final Level ref = worldRef.get();
        if (ref == null) {
            return false;
        } else if (other == null) {
            return false;
        } else if ((other instanceof CloudburstWorld)) {
            Level otherWorld = ((CloudburstWorld) other).worldRef.get();
            return ref.equals(otherWorld);
        } else if (other instanceof com.sk89q.worldedit.world.World) {
            return ((com.sk89q.worldedit.world.World) other).getName().equals(ref.getName());
        } else {
            return false;
        }
    }
}
