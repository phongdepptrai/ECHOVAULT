package com.echovault;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.Color;
import com.echovault.anim.AnimationController;

public abstract class Entity {
    public Vector2 position;
    public Vector2 velocity;
    public float rotation = 0f; // For shielded logic etc.
    public float radius; // For circular collision
    public float hp;
    public float maxHp;
    public boolean dead;
    public int team; // 0 = Player, 1 = Enemy, 2 = Neutral

    // Elite system
    public boolean isElite = false;
    public Color color = Color.WHITE;

    // Animation
    public AnimationController animController;

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
        if (animController != null) {
            animController.update(delta);
        }
    }

    public abstract void render(ShapeRenderer sr);

    public void render(SpriteBatch batch) {
        if (animController != null) {
            TextureRegion reg = animController.getRegion();
            if (reg != null) {
                batch.setColor(color);
                // Draw centered
                batch.draw(reg, position.x - reg.getRegionWidth()/2, position.y - reg.getRegionHeight()/2);
                batch.setColor(Color.WHITE);
            }
        }
    }

    public void takeDamage(float amount) {
        hp -= amount;
        if (hp <= 0) {
            hp = 0;
            dead = true;
        }
    }

    // Optional hook for subclasses to draw telegraphs
    public void renderTelegraph(ShapeRenderer sr) {}

    public Rectangle getBounds() {
        return new Rectangle(position.x - radius, position.y - radius, radius * 2, radius * 2);
    }
}
