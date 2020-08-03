package com.sk89q.worldedit.cloudburst;

import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.entity.metadata.EntityProperties;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.NullWorld;

import java.lang.ref.WeakReference;

import static com.google.common.base.Preconditions.checkNotNull;

public class CloudburstEntity implements Entity {

    private final WeakReference<cn.nukkit.entity.Entity> entityRef;

    CloudburstEntity(cn.nukkit.entity.Entity entity) {
        checkNotNull(entity);
        this.entityRef = new WeakReference<>(entity);
    }

    @Override
    public BaseEntity getState() {
        cn.nukkit.entity.Entity entity = entityRef.get();
    }

    @Override
    public boolean remove() {
        cn.nukkit.entity.Entity entity = entityRef.get();
        if(entity != null) {
            entity.despawnFromAll();
            return !entity.isAlive();
        } else {
            return true;
        }
    }

    @Override
    public Location getLocation() {
        cn.nukkit.entity.Entity entity = entityRef.get();
        if(entity != null) {
            return CloudburstAdapter.adapt(entity.getLocation());
        } else {
            return new Location(NullWorld.getInstance());
        }
    }

    @Override
    public boolean setLocation(Location location) {
        cn.nukkit.entity.Entity entity = entityRef.get();
        if(entity != null) {
            return entity.teleport(CloudburstAdapter.adapt(location));
        } else {
            return false;
        }
    }

    @Override
    public Extent getExtent() {
        cn.nukkit.entity.Entity entity = entityRef.get();
        if(entity != null) {
            return CloudburstAdapter.adapt(entity.getLevel());
        } else {
            return NullWorld.getInstance();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getFacet(Class<? extends T> cls) {
        cn.nukkit.entity.Entity entity = entityRef.get();
        if (entity != null && EntityProperties.class.isAssignableFrom(cls)) {
            return (T) new CloudburstEntityProperties(entity);
        } else {
            return null;
        }
    }
}
