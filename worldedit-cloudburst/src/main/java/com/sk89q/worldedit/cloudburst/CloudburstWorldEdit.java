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

import com.google.inject.Inject;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.event.platform.PlatformReadyEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.anvil.ChunkDeleter;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.item.ItemType;
import org.cloudburstmc.server.Server;
import org.cloudburstmc.server.command.CommandSender;
import org.cloudburstmc.server.event.Listener;
import org.cloudburstmc.server.event.server.ServerInitializationEvent;
import org.cloudburstmc.server.player.Player;
import org.cloudburstmc.server.plugin.Plugin;
import org.cloudburstmc.server.plugin.PluginContainer;
import org.cloudburstmc.server.plugin.PluginDescription;
import org.cloudburstmc.server.registry.BlockRegistry;
import org.cloudburstmc.server.registry.ItemRegistry;
import org.cloudburstmc.server.utils.Identifier;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

import static com.sk89q.worldedit.internal.anvil.ChunkDeleter.DELCHUNKS_FILE_NAME;

@Plugin(
        id = "WorldEdit",
        name = "WorldEdit",
        version = "7.2.0-SNAPSHOT"
)
public class CloudburstWorldEdit {

    private CloudburstPlatform platform;
    private CloudburstConfiguration config;
    private final Path dataFolder;
    private final Logger logger;
    private final PluginDescription description;

    private PluginContainer container;

    @Inject
    public CloudburstWorldEdit(Logger logger, PluginDescription description, Path dataDirectory) {
        this.logger = logger;
        this.description = description;
        this.dataFolder = dataDirectory;
    }

    @Listener
    public void onInitialization(ServerInitializationEvent event) {
        if (this.platform != null) {
            getLogger().warn("onEnable occurred for a second time, maybe it's reloaded? Disabling the plugin anyways");
            WorldEdit.getInstance().getPlatformManager().unregister(platform);
            // Server.getInstance().getPluginManager().disablePlugin(this);
            return;
        }

        this.container = Server.getInstance().getPluginManager().fromInstance(this).orElseThrow(() ->
                new RuntimeException("Failed to get plugin container instance"));

        if (Files.notExists(this.dataFolder)) {
            try {
                Files.createDirectory(this.dataFolder);
            } catch (IOException e) {
                throw new IllegalStateException("Could not create WorldEdit directory");
            }
        }
        final Path delChunks = dataFolder.resolve(DELCHUNKS_FILE_NAME);
        if (Files.exists(delChunks)) {
            ChunkDeleter.runFromFile(delChunks, true);
        }

        loadConfig();

        platform = new CloudburstPlatform(this);
        WorldEdit.getInstance().getPlatformManager().register(platform);

        for (org.cloudburstmc.server.block.BlockState blockState : BlockRegistry.get().getBlockStates()) {
            String identifier = blockState.getType().toString().toLowerCase(Locale.US);
            if (BlockType.REGISTRY.get(identifier) == null) {
                BlockType.REGISTRY.register(identifier, new BlockType(identifier));
            }

            if (ItemType.REGISTRY.get(identifier) == null) {
                ItemType.REGISTRY.register(identifier, new ItemType(identifier));
            }
        }

        for (Identifier itemIdentifier : ItemRegistry.get().getItems()) {
            String identifier = itemIdentifier.toString().toLowerCase(Locale.US);
            if (ItemType.REGISTRY.get(identifier) == null) {
                ItemType.REGISTRY.register(identifier, new ItemType(identifier));
            }
        }

        WorldEdit.getInstance().getEventBus().post(new PlatformReadyEvent());

        Server.getInstance().getEventManager().registerListeners(this, new CloudburstListener(this));

        getLogger().info("WorldEdit for Cloudburst (version " + description.getVersion() + ") is loaded");
    }

    public CloudburstPlatform getPlatform() {
        return platform;
    }

    public CloudburstPlayer wrapPlayer(Player player) {
        return new CloudburstPlayer(player);
    }

    public Actor wrapCommandSource(CommandSender sender) {
        if (sender instanceof Player) {
            return wrapPlayer((Player) sender);
        }

        return new CloudburstCommandSender(this, sender);
    }

    public Logger getLogger() {
        return logger;
    }

    public PluginDescription getDescription() {
        return description;
    }

    public PluginContainer getContainer() {
        return container;
    }

    public File getDataFolder() {
        return dataFolder.toFile();
    }

    private void loadConfig() {
        createDefaultConfiguration("config.yml"); // Create the default configuration file

        config = new CloudburstConfiguration(new YAMLProcessor(new File(this.dataFolder.toFile(), "config.yml"), true), this);
        config.load();
    }

    protected void createDefaultConfiguration(String name) {
        Path path = this.dataFolder.resolve(name);
        if (Files.notExists(path)) {
            try (InputStream stream = CloudburstWorldEdit.class.getClassLoader().getResourceAsStream("defaults/" + name)) {
                if (stream == null) {
                    Files.createDirectories(path.getParent());
                    Files.createFile(path);
                }
                copyDefaultConfig(stream, path, name);
            } catch (IOException e) {
                getLogger().warn("Unable to read default configuration: " + name);
            }
        }
    }

    private void copyDefaultConfig(InputStream input, Path actual, String name) {
        try {
            Files.copy(input, actual, StandardCopyOption.REPLACE_EXISTING);

            getLogger().info("Default configuration file written: " + name);
        } catch (IOException e) {
            getLogger().warn("Failed to write default config file", e);
        }
    }

    protected void loadConfiguration() {
        config.unload();
        config.load();
    }

    public CloudburstConfiguration getConfiguration() {
        return config;
    }
}
