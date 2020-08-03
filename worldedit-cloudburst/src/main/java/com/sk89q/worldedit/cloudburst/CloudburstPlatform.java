package com.sk89q.worldedit.cloudburst;

import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.registry.EntityRegistry;
import cn.nukkit.scheduler.TaskHandler;
import cn.nukkit.utils.Identifier;
import com.google.common.collect.ImmutableSet;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.*;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.world.DataFixer;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.registry.Registries;
import org.enginehub.piston.CommandManager;

import java.util.*;

public class CloudburstPlatform extends AbstractPlatform implements MultiUserPlatform {

    private final CloudburstWorldEdit plugin;

    public CloudburstPlatform(CloudburstWorldEdit plugin) {
        this.plugin = plugin;
    }

    @Override
    public Collection<Actor> getConnectedUsers() {
        return null;
    }

    @Override
    public Registries getRegistries() {

    }

    @Override
    public int getDataVersion() {
        return 2567;
    }

    @Override
    public boolean isValidMobType(String type) {
        if (!type.startsWith("minecraft:")) {
            return false;
        }
        Identifier identifier = Identifier.fromString(type);
        return EntityRegistry.get().getEntityType(identifier) != null;
    }

    @todo
    @Override
    public void reload() {
        plugin.loadConfiguration();
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public Player matchPlayer(Player player) {
        return null;
    }

    @Override
    public CloudburstWorld matchWorld(World world) {
        if(world instanceof CloudburstWorld) {
            return (CloudburstWorld) world;
        } else {
            Level level = Server.getInstance().getLevel(world.getName());
            return level != null ? new CloudburstWorld(level) : null;
        }
    }

    @todo
    @Override
    public void registerCommands(CommandManager commandManager) {

    }

    @todo
    @Override
    public void registerGameHooks() {

    }

    @Override
    public LocalConfiguration getConfiguration() {
        return null;
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
        capabilities.put(Capability.WORLDEDIT_CUI, Preference.NORMAL);
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

    @Override
    public DataFixer getDataFixer() {

    }
}
