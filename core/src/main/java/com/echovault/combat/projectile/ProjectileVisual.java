package com.echovault.combat.projectile;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.echovault.Entity;

public interface ProjectileVisual {
    void render(Entity projectile, SpriteBatch batch, float delta, float lifeTime);
}
