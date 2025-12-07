package com.echovault.combat.projectile;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.Gdx;
import com.echovault.Bullet;
import com.echovault.RoomManager;

public class ProjectileEntity extends Bullet {

    public ProjectileDef def;
    public ProjectileMotion motion;
    public ProjectileVisual visual;
    public float lifeTimeCounter = 0;

    // State for specific motions
    public Vector2 startPos;
    public Vector2 targetPos;
    public float initialAngle;
    public int bouncesLeft;
    public com.echovault.Entity homingTarget;

    public ProjectileEntity(float x, float y, float angleDeg, ProjectileDef def, int team) {
        // Super constructor sets position, radius, damage, team.
        // We override velocity handling in update() via Motion.
        super(x, y, angleDeg, def.speed, def.damage, team);
        this.def = def;
        this.radius = def.radius;
        this.lifeTime = def.lifetime;
        this.bouncesLeft = def.bounceCount;
        this.startPos = new Vector2(x, y);
        this.initialAngle = angleDeg;

        // Resolve Motion & Visual
        this.motion = ProjectileFactory.getMotion(def.motionType);
        this.visual = ProjectileFactory.getVisual(def.visualType);

        // Set initial velocity based on angle/speed for basic physics
        this.velocity.set(def.speed, 0).setAngleDeg(angleDeg);
    }

    @Override
    public void update(float delta) {
        // Do NOT call super.update(delta) if it just does pos += vel * delta,
        // because Motion might want more control.
        // But Bullet.update handles lifetime logic.
        lifeTime -= delta;
        lifeTimeCounter += delta;
        if (lifeTime <= 0) dead = true;

        // Motion Update
        // We pass 'this' which is Entity, but Motion expects Entity.
        // We need access to RoomManager to query targets/walls.
        // Bullet.update doesn't pass roomManager, but Entity.update doesn't either.
        // We need to change call signature or store RoomManager ref?
        // Storing ref is circular.
        // In EchoVault, `Entity.update` signature is `void update(float delta)`.
        // But `RoomManager` calls `((Enemy)e).update(delta, player, roomManager)`.
        // `RoomManager` iterates bullets and calls `b.update(delta)`.
        // We need to modify `RoomManager` to pass itself to bullets, or Bullet needs to handle physics simply.
        // ProjectileMotion needs RoomManager (e.g. for Homing).

        // We will do a hack: `motion.update` needs room.
        // We can't change `Bullet.update(delta)` signature easily without modifying base class.
        // Let's modify `Bullet.update(delta)` to `update(delta, RoomManager rm)`?
        // Or cast in RoomManager.
    }

    public void update(float delta, RoomManager room) {
        super.update(delta); // lifetime logic
        if (motion != null) {
            motion.update(this, room, delta, lifeTimeCounter);
        } else {
             // Fallback
             position.mulAdd(velocity, delta);
        }
    }

    @Override
    public void render(ShapeRenderer sr) {
        // Fallback or debug
        sr.setColor(team == 0 ? Color.YELLOW : Color.RED);
        sr.circle(position.x, position.y, radius);
    }

    @Override
    public void render(SpriteBatch batch) {
        if (visual != null) {
            visual.render(this, batch, Gdx.graphics.getDeltaTime(), lifeTimeCounter);
        }
    }
}
