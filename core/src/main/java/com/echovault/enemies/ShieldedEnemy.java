package com.echovault.enemies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.echovault.Bullet;
import com.echovault.Enemy;
import com.echovault.Player;
import com.echovault.RoomManager;

public class ShieldedEnemy extends Enemy {

    public ShieldedEnemy(float x, float y) {
        super(x, y, 16, 12);
        this.color = Color.TEAL;
    }

    @Override
    public void update(float delta, Player player, RoomManager roomManager) {
        if (player == null) return;
        Vector2 toPlayer = new Vector2(player.position).sub(position);

        // Move slowly
        velocity.set(toPlayer).nor().scl(60f);

        // Rotate shield to face movement (or player)
        this.rotation = velocity.angleDeg();

        super.update(delta, player, roomManager);
    }

    @Override
    public void takeDamage(float amount) {
        // Logic handled in collision usually, but since we don't have source angle here easily without refactoring 'takeDamage',
        // We will just assume this is called.
        // Wait, collision logic is in RoomManager. We need to check angle there.
        // Or we pass bullet velocity to takeDamage?
        // Let's modify Entity.takeDamage later?
        // Constraint: "Don't break existing".
        // In RoomManager, we check bullet collision. We can check angle there.
        super.takeDamage(amount);
    }

    public boolean blocksDamage(Vector2 damageSourceVelocity) {
        // Impact vector is velocity normalized.
        // Shield faces `rotation`.
        // If bullet is coming AGAINST rotation (opposing), it hits shield.
        // Bullet Angle vs Rotation.
        // If |Angle - Rotation| > 135 degrees? No, shield is FRONT.
        // Bullet moving 180 (Left). Shield facing 0 (Right). Impact.
        // Angle diff ~180.

        float bulletAngle = damageSourceVelocity.angleDeg();
        float angleDiff = Math.abs(bulletAngle - rotation);
        if (angleDiff > 180) angleDiff = 360 - angleDiff;

        // If bullet opposes shield (diff > 120), blocked.
        return angleDiff > 120;
    }

    @Override
    public void render(ShapeRenderer sr) {
        super.render(sr);
        // Draw Shield Arc
        sr.setColor(Color.BLUE);
        sr.arc(position.x, position.y, radius + 5, rotation - 45, 90);
    }
}
