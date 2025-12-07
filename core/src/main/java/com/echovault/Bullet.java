package com.echovault;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class Bullet extends Entity {
    public float damage;
    public float lifeTime = 3.0f; // Seconds

    public Bullet(float x, float y, float angleDeg, float speed, float damage, int team) {
        super(x, y, 4, 1, team); // 1 HP (irrelevant), radius 4
        this.damage = damage;
        this.velocity = new Vector2(speed, 0).setAngleDeg(angleDeg);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        lifeTime -= delta;
        if (lifeTime <= 0) {
            dead = true;
        }
    }

    @Override
    public void render(ShapeRenderer sr) {
        if (team == 0) sr.setColor(Color.YELLOW); // Player
        else if (team == 1) sr.setColor(Color.RED); // Enemy
        else sr.setColor(Color.GREEN); // Friendly Ghost

        sr.circle(position.x, position.y, radius);
    }
}
