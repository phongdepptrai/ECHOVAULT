package com.echovault.enemies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.echovault.Enemy;
import com.echovault.Player;
import com.echovault.RoomManager;
import com.echovault.utils.BulletPatterns;
import com.echovault.utils.Telegraph;

public class Bomber extends Enemy {
    private Telegraph telegraph;
    private boolean exploding = false;

    public Bomber(float x, float y) {
        super(x, y, 15, 8);
        this.color = Color.BLACK;
    }

    @Override
    public void update(float delta, Player player, RoomManager roomManager) {
        float dist = position.dst(player.position);

        if (!exploding) {
            if (dist > 80) {
                // Chase
                velocity.set(player.position).sub(position).nor().scl(110f);
            } else {
                // Plant bomb
                exploding = true;
                velocity.setZero();
                telegraph = new Telegraph(Telegraph.Type.CIRCLE, position, null, 60f, 0.8f, Color.ORANGE);
            }
        } else {
            // Run away slowly? Or sit still?
            // "then lùi ra" -> Retreat
            Vector2 away = new Vector2(position).sub(player.position).nor().scl(80f);
            velocity.set(away);

            if (telegraph.update(delta)) {
                // BOOM
                // Explosion damage AOE check
                if (player.position.dst(telegraph.position) < telegraph.radius) {
                    player.takeDamage(2);
                }
                // Visual FX needed? For now just pattern
                BulletPatterns.shootRing(roomManager, telegraph.position, 8, 150f, 1, 1, 0);

                telegraph = null;
                exploding = false; // Reset cycle
            }
        }
        super.update(delta, player, roomManager);
    }

    @Override
    public void renderTelegraph(ShapeRenderer sr) {
        if (telegraph != null) telegraph.render(sr);
    }
}
