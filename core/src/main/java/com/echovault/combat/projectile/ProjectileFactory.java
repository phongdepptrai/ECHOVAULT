package com.echovault.combat.projectile;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.echovault.Entity;
import com.echovault.RoomManager;
import com.echovault.assets.AssetGenerator;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;

import java.util.HashMap;
import java.util.Map;

public class ProjectileFactory {

    private static final Map<String, ProjectileMotion> motions = new HashMap<>();
    private static final Map<String, ProjectileVisual> visuals = new HashMap<>();

    public static void init() {
        registerMotions();
        registerVisuals();
    }

    private static void registerMotions() {
        motions.put("straight", (p, room, dt, time) -> {
            p.position.mulAdd(p.velocity, dt);
        });

        motions.put("spiral", (p, room, dt, time) -> {
            ProjectileEntity pe = (ProjectileEntity)p;
            // Velocity rotates
            p.velocity.rotateDeg(180f * dt); // Spin 180 deg per sec
            p.position.mulAdd(p.velocity, dt);
        });

        motions.put("orbit", (p, room, dt, time) -> {
            ProjectileEntity pe = (ProjectileEntity)p;
            // Orbit around startPos? Or center?
            // "Orbit fixed point"
            Vector2 center = pe.startPos;
            float radius = 50f + time * 10f; // Expand slightly
            float angle = pe.initialAngle + time * 180f;
            p.position.set(center).add(new Vector2(radius, 0).setAngleDeg(angle));
        });

        motions.put("sine", (p, room, dt, time) -> {
            ProjectileEntity pe = (ProjectileEntity)p;
            // Base movement
            Vector2 forward = new Vector2(pe.def.speed, 0).setAngleDeg(pe.initialAngle);
            Vector2 posBase = new Vector2(pe.startPos).mulAdd(forward, time);
            // Sine offset
            Vector2 perp = new Vector2(-forward.y, forward.x).nor();
            float wave = MathUtils.sin(time * 10f) * 20f;
            p.position.set(posBase).add(perp.scl(wave));
        });

        motions.put("accelerating", (p, room, dt, time) -> {
             ProjectileEntity pe = (ProjectileEntity)p;
             float speed = pe.def.speed + pe.def.accel * time;
             p.velocity.setLength(speed);
             p.position.mulAdd(p.velocity, dt);
        });

        motions.put("homing", (p, room, dt, time) -> {
            ProjectileEntity pe = (ProjectileEntity)p;
            if (pe.homingTarget == null || pe.homingTarget.dead) {
                // Find target (Player)
                if (pe.team == 1) pe.homingTarget = room.player;
            }

            if (pe.homingTarget != null) {
                Vector2 desired = new Vector2(pe.homingTarget.position).sub(p.position).nor();
                Vector2 current = new Vector2(p.velocity).nor();
                // Lerp angle
                float angle = current.angleDeg();
                float targetAngle = desired.angleDeg();
                float turn = pe.def.turnSpeed * dt;

                // Simple angle approach
                float diff = targetAngle - angle;
                while (diff < -180) diff += 360;
                while (diff > 180) diff -= 360;

                if (diff > turn) angle += turn;
                else if (diff < -turn) angle -= turn;
                else angle = targetAngle;

                p.velocity.setLength(pe.def.speed).setAngleDeg(angle);
            }
            p.position.mulAdd(p.velocity, dt);
        });

        // Add more motions as needed
    }

    private static void registerVisuals() {
        visuals.put("circle", (p, batch, dt, time) -> {
             batch.setColor(p.team == 0 ? Color.YELLOW : Color.RED);
             batch.draw(AssetGenerator.getProjectileRegion(0), p.position.x - 8, p.position.y - 8);
             batch.setColor(Color.WHITE);
        });

        visuals.put("diamond", (p, batch, dt, time) -> {
             batch.setColor(Color.MAGENTA);
             batch.draw(AssetGenerator.getProjectileRegion(1), p.position.x - 8, p.position.y - 8);
        });

        visuals.put("ring", (p, batch, dt, time) -> {
             batch.setColor(Color.CYAN);
             batch.draw(AssetGenerator.getProjectileRegion(2), p.position.x - 8, p.position.y - 8);
        });

        visuals.put("pulse", (p, batch, dt, time) -> {
             float a = 0.5f + 0.5f * MathUtils.sin(time * 10f);
             batch.setColor(1, 0, 0, a);
             batch.draw(AssetGenerator.getProjectileRegion(0), p.position.x - 8, p.position.y - 8);
             batch.setColor(Color.WHITE);
        });
    }

    public static ProjectileMotion getMotion(String key) {
        return motions.getOrDefault(key, motions.get("straight"));
    }

    public static ProjectileVisual getVisual(String key) {
        return visuals.getOrDefault(key, visuals.get("circle"));
    }

    public static ProjectileEntity create(RoomManager rm, ProjectileDef def, float x, float y, float angle, int team) {
        ProjectileEntity p = new ProjectileEntity(x, y, angle, def, team);
        rm.addBullet(p);
        return p;
    }
}
