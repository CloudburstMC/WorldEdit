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

import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.entity.metadata.EntityProperties;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.NullWorld;
import com.sk89q.worldedit.world.entity.EntityType;
import org.cloudburstmc.server.utils.Identifier;

import java.lang.ref.WeakReference;

import static com.google.common.base.Preconditions.checkNotNull;

public class CloudburstEntity implements Entity {

    private final WeakReference<org.cloudburstmc.server.entity.Entity> entityRef;

    CloudburstEntity(org.cloudburstmc.server.entity.Entity entity) {
        checkNotNull(entity);
        this.entityRef = new WeakReference<>(entity);
    }

    @Override
    public BaseEntity getState() {
        org.cloudburstmc.server.entity.Entity entity = entityRef.get();
        if (entity != null) {
            Identifier identifier = entity.getType().getIdentifier();
            return new BaseEntity(EntityType.REGISTRY.get(identifier.toString()), CloudburstAdapter.adapt(entity.getTag()));
        } else {
            return null;
        }
    }

    @Override
    public boolean remove() {
        org.cloudburstmc.server.entity.Entity entity = entityRef.get();
        if (entity != null) {
            entity.despawnFromAll();
            return !entity.isAlive();
        } else {
            return true;
        }
    }

    @Override
    public Location getLocation() {
        org.cloudburstmc.server.entity.Entity entity = entityRef.get();
        if (entity != null) {
            return CloudburstAdapter.adapt(entity.getLocation());
        } else {
            return new Location(NullWorld.getInstance());
        }
    }

    @Override
    public boolean setLocation(Location location) {
        org.cloudburstmc.server.entity.Entity entity = entityRef.get();
        if (entity != null) {
            return entity.teleport(CloudburstAdapter.adapt(location));
        } else {
            return false;
        }
    }

    @Override
    public Extent getExtent() {
        org.cloudburstmc.server.entity.Entity entity = entityRef.get();
        if (entity != null) {
            return CloudburstAdapter.adapt(entity.getLevel());
        } else {
            return NullWorld.getInstance();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getFacet(Class<? extends T> cls) {
        org.cloudburstmc.server.entity.Entity entity = entityRef.get();
        if (entity != null && EntityProperties.class.isAssignableFrom(cls)) {
            return (T) new CloudburstEntityProperties(entity);
        } else {
            return null;
        }
    }
}
