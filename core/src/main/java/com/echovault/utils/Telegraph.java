package com.echovault.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Telegraph {
    public enum Type { LINE, CIRCLE }

    public Type type;
    public Vector2 position; // Center for Circle, Start for Line
    public Vector2 target;   // End for Line (not used for Circle)
    public float radius;     // Width for Line, Radius for Circle
    public float duration;
    public float timer;
    public Color color;

    public Telegraph(Type type, Vector2 pos, Vector2 target, float radius, float duration, Color color) {
        this.type = type;
        this.position = new Vector2(pos);
        this.target = target != null ? new Vector2(target) : null;
        this.radius = radius;
        this.duration = duration;
        this.timer = duration;
        this.color = color;
    }

    public boolean update(float delta) {
        timer -= delta;
        return timer <= 0;
    }

    public void render(ShapeRenderer sr) {
        float alpha = 0.3f + (1f - (timer / duration)) * 0.4f; // Fade in opacity
        sr.setColor(color.r, color.g, color.b, alpha);

        if (type == Type.CIRCLE) {
            sr.circle(position.x, position.y, radius);
            // Draw filling ring to indicate timing
            float p = 1f - (timer / duration);
            sr.setColor(color.r, color.g, color.b, 0.8f);
            sr.circle(position.x, position.y, radius * p);
        } else if (type == Type.LINE) {
            sr.rectLine(position, target, radius);
            // Draw filling line
            float p = 1f - (timer / duration);
            Vector2 lerpEnd = new Vector2(position).lerp(target, p);
            sr.setColor(color.r, color.g, color.b, 0.8f);
            sr.rectLine(position, lerpEnd, radius * 0.5f);
        }
    }
}
