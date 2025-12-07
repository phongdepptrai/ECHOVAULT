package com.echovault;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public abstract class Entity {
    public Vector2 position;
    public Vector2 velocity;
    public float radius; // For circular collision
    public float hp;
    public float maxHp;
    public boolean dead;
    public int team; // 0 = Player, 1 = Enemy, 2 = Neutral

    public Entity(float x, float y, float radius, float hp, int team) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(0, 0);
        this.radius = radius;
        this.hp = hp;
        this.maxHp = hp;
        this.dead = false;
        this.team = team;
    }

    public void update(float delta) {
        position.mulAdd(velocity, delta);
    }

    public abstract void render(ShapeRenderer sr);

    public void takeDamage(float amount) {
        hp -= amount;
        if (hp <= 0) {
            hp = 0;
            dead = true;
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(position.x - radius, position.y - radius, radius * 2, radius * 2);
    }
}
