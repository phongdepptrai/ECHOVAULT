package com.echovault.utils;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.echovault.Bullet;
import com.echovault.RoomManager;
import java.util.Random;

public class BulletPatterns {

    // Deterministic Random helper
    private static final Vector2 temp = new Vector2();

    public static void shootAimed(RoomManager rm, Vector2 pos, Vector2 target, float speed, float damage, int team) {
        temp.set(target).sub(pos);
        rm.addBullet(new Bullet(pos.x, pos.y, temp.angleDeg(), speed, damage, team));
    }

    public static void shootRing(RoomManager rm, Vector2 pos, int count, float speed, float damage, int team, float startAngle) {
        float angleStep = 360f / count;
        for (int i = 0; i < count; i++) {
            rm.addBullet(new Bullet(pos.x, pos.y, startAngle + i * angleStep, speed, damage, team));
        }
    }

    public static void shootFan(RoomManager rm, Vector2 pos, Vector2 target, int count, float spreadDeg, float speed, float damage, int team) {
        temp.set(target).sub(pos);
        float baseAngle = temp.angleDeg();
        float startAngle = baseAngle - spreadDeg / 2;
        float step = count > 1 ? spreadDeg / (count - 1) : 0;

        for (int i = 0; i < count; i++) {
            rm.addBullet(new Bullet(pos.x, pos.y, startAngle + i * step, speed, damage, team));
        }
    }

    public static void shootSpiral(RoomManager rm, Vector2 pos, int count, float currentSpiralAngle, float speed, float damage, int team) {
         shootRing(rm, pos, count, speed, damage, team, currentSpiralAngle);
    }
}
