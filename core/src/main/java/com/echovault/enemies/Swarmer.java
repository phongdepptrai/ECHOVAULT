package com.echovault.enemies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.echovault.Enemy;
import com.echovault.Player;
import com.echovault.RoomManager;

public class Swarmer extends Enemy {
    private float zigzagOffset = 0;

    public Swarmer(float x, float y) {
        super(x, y, 8, 2); // Tiny, weak
        this.color = Color.PINK;
        this.zigzagOffset = MathUtils.random(0, 100);
    }

    @Override
    public void update(float delta, Player player, RoomManager roomManager) {
        Vector2 toPlayer = new Vector2(player.position).sub(position).nor();

        // Zigzag perpendicular
        Vector2 perp = new Vector2(-toPlayer.y, toPlayer.x);
        float wave = MathUtils.sin((stateTimer + zigzagOffset) * 10f) * 0.8f;

        velocity.set(toPlayer).add(perp.scl(wave)).nor().scl(140f);

        super.update(delta, player, roomManager);
    }
}
