package com.echovault.enemies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.echovault.Enemy;
import com.echovault.Player;
import com.echovault.RoomManager;

public class Orbiter extends Enemy {
    private float orbitAngle = 0;
    private boolean charging = false;
    private float chargeTimer = 0;

    public Orbiter(float x, float y) {
        super(x, y, 12, 5);
        this.color = Color.CYAN;
    }

    @Override
    public void update(float delta, Player player, RoomManager roomManager) {
        Vector2 toPlayer = new Vector2(player.position).sub(position);
        float dist = toPlayer.len();

        if (charging) {
            chargeTimer -= delta;
             // Charge straight
            if (chargeTimer <= 0) {
                charging = false;
            }
        } else {
             // Orbit
             float idealDist = 150f;
             Vector2 idealPos = new Vector2(player.position).add(new Vector2(idealDist, 0).setAngleDeg(orbitAngle));
             orbitAngle += delta * 100f; // Spin

             Vector2 seek = idealPos.sub(position).nor().scl(120f);
             velocity.set(seek);

             // Random charge
             if (Math.random() < 0.01) { // 1% chance per tick ~ once every 1.5s
                 charging = true;
                 chargeTimer = 1.0f;
                 velocity.set(toPlayer.nor().scl(300f));
             }
        }

        super.update(delta, player, roomManager);
    }
}
