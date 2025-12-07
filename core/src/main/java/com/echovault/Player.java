package com.echovault;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.List;

public class Player extends Entity {
    // Stats
    public float speed = 300f;
    public float dashSpeed = 1000f;
    public float dashDuration = 0.2f;
    public float dashCooldown = 1.0f;

    // State
    public float dashTimer = 0;
    public float dashCooldownTimer = 0;
    public boolean isDashing = false;

    public float fireRate = 0.2f;
    public float fireTimer = 0;
    public float damageMult = 1.0f;
    public float bulletSpeedMult = 1.0f;

    public float invulnerabilityTimer = 0;
    public float invulnerabilityDuration = 1.0f;

    // Items Logic
    public List<Item> items = new ArrayList<>();

    // Recording
    public List<InputFrame> inputHistory = new ArrayList<>();

    public Player(float x, float y) {
        super(x, y, 15, 6, 0); // HP 6 (3 hearts)
    }

    public void update(float delta, InputFrame input, RoomManager roomManager) {
        // Cooldowns
        if (dashCooldownTimer > 0) dashCooldownTimer -= delta;
        if (fireTimer > 0) fireTimer -= delta;
        if (invulnerabilityTimer > 0) invulnerabilityTimer -= delta;

        // Dashing Logic
        if (isDashing) {
            dashTimer -= delta;
            if (dashTimer <= 0) {
                isDashing = false;
                velocity.setZero();
            }
        } else {
            // Normal Movement
            Vector2 dir = new Vector2(0, 0);
            if (input.up) dir.y += 1;
            if (input.down) dir.y -= 1;
            if (input.left) dir.x -= 1;
            if (input.right) dir.x += 1;

            if (dir.len2() > 0) {
                dir.nor().scl(speed);
                velocity.set(dir);
            } else {
                velocity.setZero();
            }

            // Start Dash
            // Note: input.dash is boolean state. We need rising edge or just cooldown check.
            // Since we have cooldown, simple check is fine.
            if (input.dash && dashCooldownTimer <= 0) {
                isDashing = true;
                dashTimer = dashDuration;
                dashCooldownTimer = dashCooldown;
                // Dash towards mouse or movement dir?
                // "Right click dash" - usually towards mouse in top-down shooters or movement dir.
                // Let's go with movement dir. If stationary, dash towards mouse?
                // Let's implement dash towards movement direction. If no movement, no dash or dash forward (mouse).
                if (dir.len2() > 0) {
                   velocity.set(dir.nor().scl(dashSpeed));
                } else {
                   // Dash towards mouse
                   Vector2 mouseDir = new Vector2(1, 0).setAngleDeg(input.aimAngle);
                   velocity.set(mouseDir.scl(dashSpeed));
                }
            }
        }

        super.update(delta);

        // Shooting
        if (input.shoot && fireTimer <= 0) {
            shoot(input.aimAngle, roomManager);
            fireTimer = fireRate;
        }

        // Record Input
        // We need to copy it because the InputFrame object might be reused or modified
        inputHistory.add(new InputFrame(input));
    }

    protected void shoot(float angle, RoomManager roomManager) {
        // Bullet Spawning
        Bullet b = new Bullet(position.x, position.y, angle, 600f * bulletSpeedMult, 1 * damageMult, team);
        roomManager.addBullet(b);
    }

    @Override
    public void render(ShapeRenderer sr) {
        if (invulnerabilityTimer > 0 && ((int)(invulnerabilityTimer * 20) % 2 == 0)) {
            // Flash when hit
            return;
        }

        sr.setColor(Color.CYAN);
        sr.circle(position.x, position.y, radius);

        // Direction Indicator
        // We can't access mouse pos here easily without passing it, but we can assume logic update sets rotation?
        // Let's just draw a small line
        // We don't have the aim angle stored in the player state, it's in input.
        // Actually, we should probably store "facingAngle" in Player.
    }

    @Override
    public void takeDamage(float amount) {
        if (invulnerabilityTimer > 0) return;
        super.takeDamage(amount);
        invulnerabilityTimer = invulnerabilityDuration;
    }
}
