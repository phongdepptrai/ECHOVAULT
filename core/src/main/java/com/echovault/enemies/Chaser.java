package com.echovault.enemies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.echovault.Enemy;
import com.echovault.Player;
import com.echovault.RoomManager;

public class Chaser extends Enemy {
    public Chaser(float x, float y) {
        super(x, y, 15, 6);
        this.color = Color.ORANGE;
    }

    @Override
    public void update(float delta, Player player, RoomManager roomManager) {
        if (player == null) return;
        Vector2 toPlayer = new Vector2(player.position).sub(position).nor();
        float speed = isElite ? 130f : 100f;
        velocity.set(toPlayer.scl(speed));
        super.update(delta, player, roomManager);
    }
}
