package com.sk89q.worldedit.cloudburst;

import cn.nukkit.blockentity.ItemFrame;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.Projectile;
import cn.nukkit.entity.impl.EntityLiving;
import cn.nukkit.entity.impl.Human;
import cn.nukkit.entity.impl.passive.Animal;
import cn.nukkit.entity.impl.passive.EntityTameable;
import cn.nukkit.entity.misc.*;
import cn.nukkit.entity.passive.Bat;
import cn.nukkit.entity.passive.IronGolem;
import cn.nukkit.entity.passive.Villager;
import cn.nukkit.entity.vehicle.Boat;
import cn.nukkit.entity.vehicle.Minecart;
import cn.nukkit.entity.vehicle.TntMinecart;
import cn.nukkit.item.Item;
import cn.nukkit.player.Player;
import com.sk89q.worldedit.entity.metadata.EntityProperties;

import static com.google.common.base.Preconditions.checkNotNull;

public class CloudburstEntityProperties implements EntityProperties {

    private final Entity entity;

    CloudburstEntityProperties(Entity entity) {
        checkNotNull(entity);
        this.entity = entity;
    }

    @Override
    public boolean isPlayerDerived() {
        return entity instanceof Human;
    }

    @Override
    public boolean isProjectile() {
        return entity instanceof Projectile;
    }

    @Override
    public boolean isItem() {
        return entity instanceof Item;
    }

    @Override
    public boolean isFallingBlock() {
        return entity instanceof FallingBlock;
    }

    @Override
    public boolean isPainting() {
        return entity instanceof Painting;
    }

    @Override
    public boolean isItemFrame() {
        return entity instanceof ItemFrame;
    }

    @Override
    public boolean isBoat() {
        return entity instanceof Boat;
    }

    @Override
    public boolean isMinecart() {
        return entity instanceof Minecart;
    }

    @Override
    public boolean isTNT() {
        return entity instanceof PrimedTnt || entity instanceof TntMinecart;
    }

    @Override
    public boolean isExperienceOrb() {
        return entity instanceof ExperienceOrb;
    }

    @Override
    public boolean isLiving() {
        return entity instanceof EntityLiving;
    }

    @Override
    public boolean isAnimal() {
        return entity instanceof Animal;
    }

    @Override
    public boolean isAmbient() {
        //Looks like Cloudburst doesn't have an ambient alternative, so let's use the only mob that's ambient according to Bukkit
        return entity instanceof Bat;
    }

    @Override
    public boolean isNPC() {
        return entity instanceof Villager;
    }

    @Override
    public boolean isGolem() {
        return entity instanceof IronGolem;
    }

    @Override
    public boolean isTamed() {
        return entity instanceof EntityTameable && ((EntityTameable) entity).isTamed();
    }

    @Override
    public boolean isTagged() {
        return entity instanceof EntityLiving && entity.getNameTag() != null;
    }

    @Override
    public boolean isArmorStand() {
        return entity instanceof ArmorStand;
    }

    @Override
    public boolean isPasteable() {
        return !(entity instanceof Player/* || entity instanceof ComplexEntityPart*/);
    }
}
