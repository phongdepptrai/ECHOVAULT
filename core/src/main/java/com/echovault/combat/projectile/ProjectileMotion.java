package com.echovault.combat.projectile;

import com.badlogic.gdx.math.Vector2;
import com.echovault.Entity;
import com.echovault.RoomManager;

public interface ProjectileMotion {
    void update(Entity projectile, RoomManager room, float delta, float lifeTime);
}
