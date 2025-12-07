package com.echovault.combat.beam;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.echovault.Entity;
import com.echovault.RoomManager;
import com.echovault.assets.AssetGenerator;

public class Beam extends Entity {
    public float telegraphDuration = 0.8f;
    public float fireDuration = 0.4f;
    public float damagePerTick = 1f;
    public float tickRate = 0.1f;

    public float angle;
    public float length = 1000f;
    public float width = 20f;
    public boolean rotating = false;
    public float rotateSpeed = 0f;

    private float timer = 0;
    private float tickTimer = 0;
    private Vector2 end = new Vector2();
    private Vector2 start = new Vector2();

    public Beam(float x, float y, float angle, float length, float width, int team) {
        super(x, y, 0, 1, team); // radius 0, hp 1 (indestructible/timed)
        this.angle = angle;
        this.length = length;
        this.width = width;
        this.invulnerable = true;
    }

    // Quick fix for Entity not having invulnerable flag in base class
    public boolean invulnerable = true;

    @Override
    public void update(float delta) {
        // Logic handled in update(delta, room) usually?
        // Entity.update(delta) is called by RoomManager.
        // But for beams we need room manager for collision.
        // We'll do it in override update with roomManager if we refactor RoomManager,
        // or just use collision logic in RoomManager?
        // RoomManager's loop checks collision for BULLETS. Beams are ENTITIES.
        // Entities collision logic is usually Enemy vs Player.
        // We need Beam logic to hurt Player.

        // This requires Beam to be in `entities` list.
        timer += delta;
        if (rotating) {
            angle += rotateSpeed * delta;
        }

        start.set(position);
        end.set(length, 0).setAngleDeg(angle).add(start);

        if (timer >= telegraphDuration) {
            // Fire Phase
            if (timer > telegraphDuration + fireDuration) {
                dead = true;
            } else {
                tickTimer -= delta;
                if (tickTimer <= 0) {
                    tickTimer = tickRate;
                    // Check collision manually?
                    // We can't access RoomManager here easily.
                    // But RoomManager loops entities.
                    // We can mark this entity as "Active Hazard".
                }
            }
        }
    }

    public void checkCollision(RoomManager room) {
        if (timer < telegraphDuration) return; // Telegraphing

        // Check Player
        if (room.player != null) {
            // Circle-Segment
            float dist = Intersector.distanceSegmentPoint(start, end, room.player.position);
            if (dist < width/2 + room.player.radius) {
                room.player.takeDamage(damagePerTick);
            }
        }
    }

    @Override
    public void render(ShapeRenderer sr) {
        // Fallback
    }

    @Override
    public void render(SpriteBatch batch) {
        if (timer < telegraphDuration) {
            // Telegraph
            float alpha = 0.3f + 0.2f * MathUtils.sin(timer * 20f);
            batch.setColor(1, 0, 0, alpha);
            // Draw rotated rect
            drawBeam(batch, width * 0.5f);
        } else {
            // Fire
            float alpha = 0.8f + 0.2f * MathUtils.sin(timer * 50f);
            batch.setColor(1, 0.5f, 0.5f, alpha);
            drawBeam(batch, width);

            // Core
            batch.setColor(Color.WHITE);
            drawBeam(batch, width * 0.3f);
        }
        batch.setColor(Color.WHITE);
    }

    private void drawBeam(SpriteBatch batch, float w) {
        // Use projectile sheet region 3 (beam glow)
        // We need to stretch it.
        // TextureRegion, x, y, originX, originY, width, height, scaleX, scaleY, rotation
        batch.draw(AssetGenerator.getProjectileRegion(3),
                   position.x, position.y - w/2,
                   0, w/2,
                   length, w,
                   1, 1,
                   angle);
    }
}
