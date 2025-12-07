package com.echovault.enemies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.echovault.Enemy;
import com.echovault.Player;
import com.echovault.RoomManager;
import com.echovault.utils.Telegraph;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Hopper extends Enemy {
    private float jumpCooldown = 1.0f;
    private Vector2 targetPos = new Vector2();
    private boolean isJumping = false;
    private Telegraph telegraph;

    public Hopper(float x, float y) {
        super(x, y, 15, 8);
        this.color = Color.GREEN;
    }

    @Override
    public void update(float delta, Player player, RoomManager roomManager) {
        if (isJumping) {
            // In air / landing logic
            float dist = targetPos.dst(position);
            if (dist < 5f) {
                isJumping = false;
                velocity.setZero();
                jumpCooldown = isElite ? 0.6f : 1.2f;
                // Knockback check? handled by collision usually
            } else {
                velocity.set(targetPos).sub(position).nor().scl(400f); // Fast jump
            }
        } else {
            velocity.setZero();
            jumpCooldown -= delta;

            if (jumpCooldown <= 0.4f && telegraph == null) {
                 // Start telegraphing jump
                 telegraph = new Telegraph(Telegraph.Type.CIRCLE, player.position, null, 20f, 0.4f, Color.RED);
                 targetPos.set(player.position);
            }

            if (telegraph != null) {
                if (telegraph.update(delta)) {
                    // Jump!
                    isJumping = true;
                    telegraph = null;
                }
            } else if (jumpCooldown <= 0) {
                // Failsafe if logic glitches
                jumpCooldown = 1.2f;
            }
        }
        super.update(delta, player, roomManager);
    }

    @Override
    public void renderTelegraph(ShapeRenderer sr) {
        if (telegraph != null) telegraph.render(sr);
    }
}
