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
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.cui.CUIEvent;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.util.formatting.WorldEditText;
import com.sk89q.worldedit.util.formatting.text.Component;
import org.cloudburstmc.server.command.CommandSender;
import org.cloudburstmc.server.player.Player;
import org.cloudburstmc.server.utils.TextFormat;

import java.io.File;
import java.util.Locale;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class CloudburstCommandSender implements Actor {

    /**
     * One time generated ID.
     */
    private static final UUID DEFAULT_ID = UUID.fromString("a233eb4b-4cab-42cd-9fd9-7e7b9a3f74be");

    private final CommandSender sender;
    private final CloudburstWorldEdit plugin;

    public CloudburstCommandSender(CloudburstWorldEdit plugin, CommandSender sender) {
        checkNotNull(plugin);
        checkNotNull(sender);
        checkArgument(!(sender instanceof Player), "Cannot wrap a player");

        this.plugin = plugin;
        this.sender = sender;
    }

    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
    public void printRaw(String msg) {
        for (String part : msg.split("\n")) {
            sender.sendMessage(TextFormat.colorize(msg));
        }
    }

    @Override
    public void printDebug(String msg) {
        sendColorized(msg, TextFormat.GRAY);
    }

    @Override
    public void print(String msg) {
        sendColorized(msg, TextFormat.LIGHT_PURPLE);
    }

    @Override
    public void printError(String msg) {
        sendColorized(msg, TextFormat.RED);
    }

    @Override
    public void print(Component component) {
        sender.sendMessage(WorldEditText.reduceToText(component, getLocale()));
    }

    private void sendColorized(String msg, TextFormat formatting) {
        for (String part : msg.split("\n")) {
            sender.sendMessage(formatting + TextFormat.colorize(part));
        }
    }

    @Override
    public boolean canDestroyBedrock() {
        return true;
    }

    @Override
    public boolean isPlayer() {
        return false;
    }

    @Override
    public File openFileOpenDialog(String[] extensions) {
        return null;
    }

    @Override
    public File openFileSaveDialog(String[] extensions) {
        return null;
    }

    @Override
    public void dispatchCUIEvent(CUIEvent event) {
    }

    @Override
    public Locale getLocale() {
        return WorldEdit.getInstance().getConfiguration().defaultLocale;
    }

    @Override
    public SessionKey getSessionKey() {
        return new SessionKey() {
            @Override
            public String getName() {
                return null;
            }

            @Override
            public boolean isActive() {
                return false;
            }

            @Override
            public boolean isPersistent() {
                return false;
            }

            @Override
            public UUID getUniqueId() {
                return DEFAULT_ID;
            }
        };
    }

    @Override
    public UUID getUniqueId() {
        return DEFAULT_ID;
    }

    @Override
    public String[] getGroups() {
        return new String[0];
    }

    @Override
    public void checkPermission(String permission) {
    }

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }
}
