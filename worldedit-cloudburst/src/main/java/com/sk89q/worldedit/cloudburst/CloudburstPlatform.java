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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.event.platform.CommandEvent;
import com.sk89q.worldedit.extension.platform.AbstractPlatform;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.MultiUserPlatform;
import com.sk89q.worldedit.extension.platform.Preference;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.registry.Registries;
import org.cloudburstmc.server.Server;
import org.cloudburstmc.server.command.CommandSender;
import org.cloudburstmc.server.command.data.CommandData;
import org.cloudburstmc.server.level.Level;
import org.cloudburstmc.server.registry.EntityRegistry;
import org.cloudburstmc.server.scheduler.TaskHandler;
import org.cloudburstmc.server.utils.Identifier;
import org.enginehub.piston.CommandManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CloudburstPlatform extends AbstractPlatform implements MultiUserPlatform {

    private final CloudburstWorldEdit plugin;
    private boolean hookingEvents = false;

    public CloudburstPlatform(CloudburstWorldEdit plugin) {
        this.plugin = plugin;
    }

    public boolean isHookingEvents() {
        return hookingEvents;
    }

    @Override
    public Collection<Actor> getConnectedUsers() {
        List<Actor> users = new ArrayList<>();
        for (org.cloudburstmc.server.player.Player player : plugin.getServer().getOnlinePlayers().values()) {
            users.add(plugin.wrapPlayer(player));
        }
        return users;
    }

    @Override
    public Registries getRegistries() {
        return CloudburstRegistries.getInstance();
    }

    //TODO If there is something internally
    @Override
    public int getDataVersion() {
        return -1;
    }

    @Override
    public boolean isValidMobType(String type) {
        if (!type.startsWith("minecraft:")) {
            return false;
        }
        Identifier identifier = Identifier.fromString(type);
        return EntityRegistry.get().getEntityType(identifier) != null;
    }

    @Override
    public void reload() {
        plugin.loadConfiguration();
    }

    @Override
    public Player matchPlayer(Player player) {
        if (player instanceof CloudburstPlayer) {
            return player;
        } else {
            org.cloudburstmc.server.player.Player cloudburstPlayer = plugin.getServer().getPlayerExact(player.getName());
            return cloudburstPlayer != null ? plugin.wrapPlayer(cloudburstPlayer) : null;
        }
    }

    @Override
    public CloudburstWorld matchWorld(World world) {
        if (world instanceof CloudburstWorld) {
            return (CloudburstWorld) world;
        } else {
            Level level = Server.getInstance().getLevel(world.getName());
            return level != null ? new CloudburstWorld(level) : null;
        }
    }

    //TODO Clean this up
    @Override
    public void registerCommands(CommandManager commandManager) {
        plugin.getLogger().info("Registering commands!");
        commandManager.getAllCommands().forEach(command -> {
            String name = command.getName().replaceAll("!", "");
            /*if (name.isEmpty()) {
                name = "/worldedit";
            }*/

            List<String> aliases = new ArrayList<>();
            for (int i = 0; i < command.getAliases().size(); i++) {
                String alias = command.getAliases().get(i).replaceAll("!", "").replaceAll(",", "").replaceAll(";", "");
                if (!alias.isEmpty()) {
                    aliases.add(alias);
                }
            }

            final String finalName = name;
            org.cloudburstmc.server.command.Command cloudburstCommand = new org.cloudburstmc.server.command.Command(CommandData.builder(finalName).addAliases(aliases.toArray(new String[0])).build()) {
                @Override
                public boolean execute(CommandSender commandSender, String s, String[] strings) {
                    String arguments = rebuildArguments(s, strings);
                    CommandEvent event = new CommandEvent(plugin.wrapCommandSource(commandSender), arguments);
                    WorldEdit.getInstance().getEventBus().post(event);
                    return true;
                }
            };

            plugin.getLogger().info("Registering " + name);

            plugin.getServer().getCommandRegistry().register(plugin, cloudburstCommand);
        });
    }

    private String rebuildArguments(String commandLabel, String[] args) {
        int plSep = commandLabel.indexOf(":");
        if (plSep >= 0 && plSep < commandLabel.length() + 1) {
            commandLabel = commandLabel.substring(plSep + 1);
        }

        StringBuilder sb = new StringBuilder("/").append(commandLabel);
        if (args.length > 0) {
            sb.append(" ");
        }
        return Joiner.on(" ").appendTo(sb, args).toString();
    }

    @Override
    public void registerGameHooks() {
        hookingEvents = true;
    }

    @Override
    public LocalConfiguration getConfiguration() {
        return plugin.getConfiguration();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String getPlatformName() {
        return "Cloudburst-Official";
    }

    @Override
    public String getPlatformVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public Map<Capability, Preference> getCapabilities() {
        Map<Capability, Preference> capabilities = new EnumMap<>(Capability.class);
        capabilities.put(Capability.CONFIGURATION, Preference.NORMAL);
        capabilities.put(Capability.WORLDEDIT_CUI, Preference.PREFER_OTHERS);
        capabilities.put(Capability.GAME_HOOKS, Preference.NORMAL);
        capabilities.put(Capability.PERMISSIONS, Preference.NORMAL);
        capabilities.put(Capability.USER_COMMANDS, Preference.NORMAL);
        capabilities.put(Capability.WORLD_EDITING, Preference.PREFERRED);
        return capabilities;
    }

    @Override
    public Set<SideEffect> getSupportedSideEffects() {
        return ImmutableSet.of();
    }

    @Override
    public int schedule(long delay, long period, Runnable task) {
        TaskHandler taskHandler = plugin.getServer().getScheduler().scheduleDelayedRepeatingTask(plugin,
                task,
                (int) delay,
                (int) period);
        return taskHandler.getTaskId();
    }

    @Override
    public List<World> getWorlds() {
        Set<Level> worlds = plugin.getServer().getLevels();
        List<World> ret = new ArrayList<>(worlds.size());

        for (Level level : worlds) {
            ret.add(CloudburstAdapter.adapt(level));
        }

        return ret;
    }
}
