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

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extension.platform.AbstractPlayerActor;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.internal.cui.CUIEvent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.formatting.WorldEditText;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.gamemode.GameModes;
import org.cloudburstmc.server.AdventureSettings;
import org.cloudburstmc.server.Server;
import org.cloudburstmc.server.item.Item;
import org.cloudburstmc.server.player.Player;
import org.cloudburstmc.server.utils.TextFormat;

import java.util.Locale;
import java.util.UUID;
import javax.annotation.Nullable;

public class CloudburstPlayer extends AbstractPlayerActor {

    private final Player player;

    public CloudburstPlayer(Player player) {
        this.player = player;
    }

    @Override
    public UUID getUniqueId() {
        return player.getServerId();
    }

    @Override
    public BaseItemStack getItemInHand(HandSide handSide) {
        Item item = handSide == HandSide.MAIN_HAND ? player.getInventory().getItemInHand() : player.getInventory().getOffHand();
        return CloudburstAdapter.adapt(item);
    }

    @Override
    public BaseBlock getBlockInHand(HandSide handSide) throws WorldEditException {
        Item item = handSide.equals(HandSide.MAIN_HAND) ? player.getInventory().getItemInHand() : player.getInventory().getOffHand();
        return CloudburstAdapter.asBlockState(item).toBaseBlock();
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public String getDisplayName() {
        return player.getDisplayName();
    }

    @Override
    public void giveItem(BaseItemStack itemStack) {
        player.getInventory().addItem(CloudburstAdapter.adapt(itemStack));
    }

    @Override
    @Deprecated
    public void printRaw(String msg) {
        for (String part : msg.split("\n")) {
            player.sendMessage(part);
        }
    }

    @Override
    @Deprecated
    public void print(String msg) {
        for (String part : msg.split("\n")) {
            player.sendMessage("§d" + part);
        }
    }

    @Override
    @Deprecated
    public void printDebug(String msg) {
        for (String part : msg.split("\n")) {
            player.sendMessage("§7" + part);
        }
    }

    @Override
    @Deprecated
    public void printError(String msg) {
        for (String part : msg.split("\n")) {
            player.sendMessage("§c" + part);
        }
    }

    @Override
    public void print(Component component) {
        player.sendMessage(TextFormat.colorize(WorldEditText.reduceToText(component, getLocale())));
    }

    @Override
    public boolean trySetPosition(Vector3 pos, float pitch, float yaw) {
        return player.teleport(CloudburstAdapter.adapt(new Location(CloudburstAdapter.adapt(player.getLevel()), pos, pitch, yaw)));
    }

    @Override
    public String[] getGroups() {
        return new String[0];
    }

    @Override
    public BlockBag getInventoryBlockBag() {
        return null;
    }

    @Override
    public boolean hasPermission(String permission) {
        return player.hasPermission(permission);
    }

    @Override
    public <T> T getFacet(Class<? extends T> cls) {
        return null;
    }

    @Override
    public GameMode getGameMode() {
        return GameModes.get(player.getGamemode().getName().toLowerCase(getLocale()));
    }

    @Override
    public void setGameMode(GameMode gameMode) {
        player.setGamemode(org.cloudburstmc.server.player.GameMode.from(gameMode.getId()));
    }

    @Override
    public World getWorld() {
        return CloudburstAdapter.adapt(player.getLevel());
    }

    @Override
    public void dispatchCUIEvent(CUIEvent event) {
    }

    @Override
    public boolean isAllowedToFly() {
        return player.getAdventureSettings().get(AdventureSettings.Type.ALLOW_FLIGHT);
    }

    @Override
    protected void setFlying(boolean flying) {
        player.getAdventureSettings().set(AdventureSettings.Type.FLYING, flying);
        player.getAdventureSettings().update();
    }

    @Override
    public <B extends BlockStateHolder<B>> void sendFakeBlock(BlockVector3 pos, B block) {
        //TODO ?
    }

    @Override
    public Locale getLocale() {
        return player.getLocale();
    }

    @Override
    public BaseEntity getState() {
        throw new UnsupportedOperationException("Cannot create a state from this object");
    }

    @Override
    public Location getLocation() {
        return CloudburstAdapter.adapt(player.getLocation());
    }

    @Override
    public boolean setLocation(Location location) {
        return player.teleport(CloudburstAdapter.adapt(location));
    }

    @Override
    public SessionKey getSessionKey() {
        return new SessionKeyImpl(player.getServerId(), player.getName());
    }

    private static class SessionKeyImpl implements SessionKey {
        // If not static, this will leak a reference

        private final UUID uuid;
        private final String name;

        private SessionKeyImpl(UUID uuid, String name) {
            this.uuid = uuid;
            this.name = name;
        }

        @Override
        public UUID getUniqueId() {
            return uuid;
        }

        @Nullable
        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isActive() {
            return Server.getInstance().getPlayer(uuid).isPresent();
        }

        @Override
        public boolean isPersistent() {
            return true;
        }
    }
}
