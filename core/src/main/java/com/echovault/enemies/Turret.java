package com.echovault.enemies;

import com.badlogic.gdx.graphics.Color;
import com.echovault.Enemy;
import com.echovault.Player;
import com.echovault.RoomManager;
import com.echovault.utils.BulletPatterns;

public class Turret extends Enemy {
    private float fireTimer = 2.0f;
    private boolean alternate = false;

    public Turret(float x, float y) {
        super(x, y, 18, 15); // Tanky
        this.color = Color.MAGENTA;
    }

    @Override
    public void update(float delta, Player player, RoomManager roomManager) {
        velocity.setZero(); // Static
        fireTimer -= delta;
        if (fireTimer <= 0) {
            if (isElite) {
                // Elite: Ring shot
                BulletPatterns.shootRing(roomManager, position, 8, 200f, 1, 1, alternate ? 0 : 22.5f);
                alternate = !alternate;
            } else {
                // Normal: Aimed stream
                BulletPatterns.shootAimed(roomManager, position, player.position, 250f, 1, 1);
            }
            fireTimer = 1.5f;
        }
        super.update(delta, player, roomManager);
    }
}
