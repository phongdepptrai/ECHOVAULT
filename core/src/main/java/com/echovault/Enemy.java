package com.echovault;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class Enemy extends Entity {
    public int type; // 0=Chaser, 1=Shooter, 2=Splitter
    public float shootTimer = 0;

    public Enemy(float x, float y, int type) {
        super(x, y, 15, 5, 1); // 5 HP, team 1 (Enemy)
        this.type = type;
        if (type == 2) { // Splitter
            radius = 20;
            maxHp = 8;
            hp = 8;
        }
    }

    public void update(float delta, Player player, RoomManager roomManager) {
        if (player == null) return;

        Vector2 toPlayer = new Vector2(player.position).sub(position);
        float dist = toPlayer.len();
        toPlayer.nor();

        if (type == 0) { // Chaser
            velocity.set(toPlayer.scl(100f));
        } else if (type == 1) { // Shooter
            // Keep distance ~200
            if (dist > 250) {
                velocity.set(toPlayer.scl(80f));
            } else if (dist < 150) {
                velocity.set(toPlayer.scl(-80f));
            } else {
                velocity.setZero();
            }

            shootTimer -= delta;
            if (shootTimer <= 0) {
                shootTimer = 2.0f;
                // Shoot at player
                Vector2 aim = new Vector2(player.position).sub(position);
                Bullet b = new Bullet(position.x, position.y, aim.angleDeg(), 400f, 1, team);
                roomManager.addBullet(b);
            }
        } else if (type == 2) { // Splitter
            velocity.set(toPlayer.scl(60f));
        }

        super.update(delta);
    }

    @Override
    public void takeDamage(float amount) {
        super.takeDamage(amount);
    }

    public void onDeath(RoomManager roomManager) {
        if (type == 2) {
            // Spawn 2 small chasers
            roomManager.addEnemy(new Enemy(position.x - 10, position.y, 0));
            roomManager.addEnemy(new Enemy(position.x + 10, position.y, 0));
        }
    }

    @Override
    public void render(ShapeRenderer sr) {
        if (type == 0) sr.setColor(Color.ORANGE);
        else if (type == 1) sr.setColor(Color.MAGENTA);
        else sr.setColor(Color.PURPLE);

        sr.circle(position.x, position.y, radius);
    }
}
