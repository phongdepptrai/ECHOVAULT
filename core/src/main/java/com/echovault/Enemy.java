package com.echovault;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

// Base class for all enemies
public class Enemy extends Entity {
    protected float stateTimer = 0;
    protected int state = 0; // Generic state machine index

    public Enemy(float x, float y, float radius, float hp) {
        super(x, y, radius, hp, 1); // Team 1
        this.color = Color.RED;

        // Init default animation if available
        com.echovault.anim.AnimationSet set = com.echovault.anim.AnimationBank.getSet("enemy_generic");
        if (set != null) {
            this.animController = new com.echovault.anim.AnimationController(set);
        }
    }

    // Deprecated constructor for compatibility if needed, but we will refactor usage
    public Enemy(float x, float y, int type) {
        super(x, y, 15, 5, 1);
        // This is legacy, shouldn't be used by new code
    }

    public void update(float delta, Player player, RoomManager roomManager) {
        super.update(delta);
        stateTimer += delta;
    }

    public void onDeath(RoomManager roomManager) {
        // Base behavior (drops, etc could go here)
    }

    @Override
    public void render(ShapeRenderer sr) {
        // Fallback or debug
        if (isElite) {
            sr.setColor(Color.GOLD);
            sr.circle(position.x, position.y, radius + 2);
        }
        sr.setColor(color);
        sr.circle(position.x, position.y, radius);
    }

    public void makeElite() {
        this.isElite = true;
        this.maxHp *= 1.5f;
        this.hp = maxHp;
    }
}
