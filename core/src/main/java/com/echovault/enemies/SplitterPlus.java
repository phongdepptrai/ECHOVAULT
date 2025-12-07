package com.echovault.enemies;

import com.badlogic.gdx.graphics.Color;
import com.echovault.Enemy;
import com.echovault.RoomManager;

public class SplitterPlus extends Enemy {
    public SplitterPlus(float x, float y) {
        super(x, y, 20, 12);
        this.color = Color.PURPLE;
    }

    // Basic chaser logic for parent
    @Override
    public void update(float delta, com.echovault.Player player, RoomManager roomManager) {
        if (player == null) return;
        velocity.set(player.position).sub(position).nor().scl(70f); // Slower, tankier
        super.update(delta, player, roomManager);
    }

    @Override
    public void onDeath(RoomManager roomManager) {
        // Spawn 3 Swarmers instead of generic enemies
        for (int i=0; i<3; i++) {
             Enemy e = new Swarmer(position.x + (i-1)*15, position.y + (i-1)*15);
             roomManager.addEnemy(e);
        }
    }
}
