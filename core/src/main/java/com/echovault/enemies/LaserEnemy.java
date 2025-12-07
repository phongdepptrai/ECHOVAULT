package com.echovault.enemies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.echovault.Enemy;
import com.echovault.Player;
import com.echovault.RoomManager;
import com.echovault.utils.Telegraph;

public class LaserEnemy extends Enemy {
    private Telegraph telegraph;
    private float cycleTimer = 2.0f;
    private boolean firing = false;
    private Vector2 laserEnd = new Vector2();

    public LaserEnemy(float x, float y) {
        super(x, y, 14, 8);
        this.color = Color.MAROON;
    }

    @Override
    public void update(float delta, Player player, RoomManager roomManager) {
        velocity.setZero(); // Static sniper

        cycleTimer -= delta;

        if (cycleTimer <= 1.15f && !firing && telegraph == null) {
            // Start Telegraph (0.9s)
            Vector2 dir = new Vector2(player.position).sub(position).nor().scl(600f); // 600 length laser
            laserEnd.set(position).add(dir);
            telegraph = new Telegraph(Telegraph.Type.LINE, position, laserEnd, 4f, 0.9f, Color.RED);
        }

        if (telegraph != null) {
             if (telegraph.update(delta)) {
                 // Fire! (0.25s duration logic handled by visual or instant?)
                 // "Hitscan in 0.25s".
                 // We can do instant damage tick.
                 firing = true;
                 telegraph = null; // Teleg done, now fire
                 cycleTimer = 0.25f; // Reuse timer for fire duration

                 // Check collision once or per tick?
                 // Simple Line-Circle intersection
                 if (interdicts(position, laserEnd, player.position, player.radius)) {
                     player.takeDamage(1);
                 }
             }
        } else if (firing) {
             // Just visual duration
             if (cycleTimer <= 0) {
                 firing = false;
                 cycleTimer = isElite ? 1.5f : 2.5f;
             }
        }

        super.update(delta, player, roomManager);
    }

    private boolean interdicts(Vector2 a, Vector2 b, Vector2 c, float r) {
        // Line segment AB vs Circle C,r
        // LibGDX Intersector class is useful but simple math here:
        // Project C onto Line AB.
        Vector2 ac = new Vector2(c).sub(a);
        Vector2 ab = new Vector2(b).sub(a);
        float t = ac.dot(ab) / ab.len2();
        if (t < 0) t = 0;
        if (t > 1) t = 1;
        Vector2 closest = new Vector2(a).add(ab.scl(t));
        return closest.dst(c) < r;
    }

    @Override
    public void render(ShapeRenderer sr) {
        super.render(sr);
        if (firing) {
            sr.setColor(Color.WHITE);
            sr.rectLine(position, laserEnd, 6f);
            sr.setColor(Color.RED);
            sr.rectLine(position, laserEnd, 3f);
        }
    }

    @Override
    public void renderTelegraph(ShapeRenderer sr) {
        if (telegraph != null) telegraph.render(sr);
    }
}
