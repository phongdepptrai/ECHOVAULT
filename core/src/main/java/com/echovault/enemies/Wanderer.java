package com.echovault.enemies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.echovault.Enemy;
import com.echovault.Player;
import com.echovault.RoomManager;
import com.echovault.utils.BulletPatterns;

public class Wanderer extends Enemy {
    private float dirChangeTimer = 0;
    private float shootTimer = 0;

    public Wanderer(float x, float y) {
        super(x, y, 15, 10);
        this.color = Color.GRAY;
    }

    @Override
    public void update(float delta, Player player, RoomManager roomManager) {
        // Move randomly
        dirChangeTimer -= delta;
        if (dirChangeTimer <= 0) {
            float angle = MathUtils.random(360f);
            velocity.set(1, 0).setAngleDeg(angle).scl(50f);
            dirChangeTimer = MathUtils.random(1.0f, 3.0f);
        }

        // Shoot if player nearby
        float dist = position.dst(player.position);
        if (dist < 300) {
            shootTimer -= delta;
            if (shootTimer <= 0) {
                BulletPatterns.shootFan(roomManager, position, player.position, 3, 30f, 250f, 1, 1);
                shootTimer = isElite ? 1.5f : 2.5f;
            }
        }

        super.update(delta, player, roomManager);
    }
}
