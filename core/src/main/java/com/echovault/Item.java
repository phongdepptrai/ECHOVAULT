package com.echovault;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class Item {
    public float x, y;
    public ItemType type;
    public float radius = 10;

    public Item(float x, float y, ItemType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public void apply(Player p) {
        switch (type) {
            case DAMAGE_UP:
                p.damageMult += 0.5f;
                break;
            case FIRE_RATE_UP:
                p.fireRate = Math.max(0.05f, p.fireRate * 0.8f);
                break;
            case SPEED_UP:
                p.speed *= 1.1f;
                break;
            case BULLET_SPEED_UP:
                p.bulletSpeedMult *= 1.2f;
                break;
            case MAX_HP_UP:
                p.maxHp += 2;
                p.hp += 2;
                break;
            case DASH_COOLDOWN_DOWN:
                p.dashCooldown *= 0.8f;
                break;
            case FRIENDLY_ECHO:
            case DELAYED_ECHO:
            case FORKED_ECHO:
                // Logic handled in Ghost spawning
                break;
            default: break;
        }
    }

    public void render(ShapeRenderer sr) {
        sr.setColor(Color.GOLD);
        sr.circle(x, y, radius);
    }

    public Rectangle getBounds() {
        return new Rectangle(x - radius, y - radius, radius * 2, radius * 2);
    }
}
