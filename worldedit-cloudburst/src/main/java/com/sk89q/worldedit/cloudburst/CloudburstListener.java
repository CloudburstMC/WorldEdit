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

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import org.cloudburstmc.server.block.Block;
import org.cloudburstmc.server.event.EventHandler;
import org.cloudburstmc.server.event.EventPriority;
import org.cloudburstmc.server.event.Listener;
import org.cloudburstmc.server.event.player.PlayerGameModeChangeEvent;
import org.cloudburstmc.server.event.player.PlayerInteractEvent;

public class CloudburstListener implements Listener {

    private final CloudburstWorldEdit plugin;

    public CloudburstListener(CloudburstWorldEdit plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!plugin.getPlatform().isHookingEvents()) {
            return;
        }

        WorldEdit worldEdit = WorldEdit.getInstance();
        Player player = plugin.wrapPlayer(event.getPlayer());
        World world = player.getWorld();
        Direction direction = CloudburstAdapter.adapt(event.getFace());

        if (event.getAction().equals(PlayerInteractEvent.Action.LEFT_CLICK_BLOCK)) {
            Block clickedBlock = event.getBlock();
            Location location = new Location(world, clickedBlock.getX(), clickedBlock.getY(), clickedBlock.getZ());

            if (worldEdit.handleBlockLeftClick(player, location, direction)) {
                event.setCancelled(true);
            }

            if (worldEdit.handleArmSwing(player)) {
                event.setCancelled(true);
            }
        } else if (event.getAction().equals(PlayerInteractEvent.Action.LEFT_CLICK_AIR)) {
            if (worldEdit.handleArmSwing(player)) {
                event.setCancelled(true);
            }
        } else if (event.getAction().equals(PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)) {
            Block clickedBlock = event.getBlock();
            Location location = new Location(world, clickedBlock.getX(), clickedBlock.getY(), clickedBlock.getZ());

            if (worldEdit.handleBlockRightClick(player, location, direction)) {
                event.setCancelled(true);
            }

            if (worldEdit.handleRightClick(player)) {
                event.setCancelled(true);
            }
        } else if (event.getAction().equals(PlayerInteractEvent.Action.RIGHT_CLICK_AIR)) {
            if (worldEdit.handleRightClick(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGamemodeChange(PlayerGameModeChangeEvent event) {
        if (!plugin.getPlatform().isHookingEvents()) {
            return;
        }

        WorldEdit.getInstance().getSessionManager().get(plugin.wrapPlayer(event.getPlayer()));
    }
}
