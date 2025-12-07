package com.echovault.pattern;

import com.badlogic.gdx.math.Vector2;
import com.echovault.RoomManager;
import com.echovault.combat.projectile.ProjectileDef;
import com.echovault.combat.projectile.ProjectileFactory;
import com.echovault.combat.beam.Beam;
import com.echovault.core.Registry;
import java.util.HashMap;
import java.util.Map;

public class PatternFactory {
    private static final Map<String, AttackPattern> patterns = new HashMap<>();

    public static void init() {
        // Helpers
        patterns.put("fan_5", (room, src, target) -> {
            ProjectileDef def = getDef("basic_enemy");
            shootFan(room, src, target, def, 5, 45f);
        });

        patterns.put("ring_12", (room, src, target) -> {
            ProjectileDef def = getDef("basic_enemy");
            shootRing(room, src, def, 12, 0f);
        });

        patterns.put("spiral_rapid", (room, src, target) -> {
             ProjectileDef def = getDef("spiral_bullet");
             // Just one bullet, boss calls this repeatedly
             // Or logic handles burst?
             // Let's assume pattern executes ONE instance of the logic.
             // If spiral needs state, the caller handles timer.
             // Here we shoot 1 bullet with spiral motion.
             ProjectileFactory.create(room, def, src.x, src.y, 0, 1);
        });

        patterns.put("beam_sweep", (room, src, target) -> {
             Beam b = new Beam(src.x, src.y, 0, 1000, 30, 1);
             b.rotating = true;
             b.rotateSpeed = 45f;
             room.entities.add(b);
        });
    }

    // Helpers
    public static void shootFan(RoomManager room, Vector2 src, Vector2 target, ProjectileDef def, int count, float spread) {
        float baseAngle = new Vector2(target).sub(src).angleDeg();
        float start = baseAngle - spread/2;
        float step = spread / (count-1);
        for(int i=0; i<count; i++) {
            ProjectileFactory.create(room, def, src.x, src.y, start + i*step, 1);
        }
    }

    public static void shootRing(RoomManager room, Vector2 src, ProjectileDef def, int count, float offset) {
        float step = 360f / count;
        for(int i=0; i<count; i++) {
             ProjectileFactory.create(room, def, src.x, src.y, offset + i*step, 1);
        }
    }

    private static ProjectileDef getDef(String key) {
        return (ProjectileDef)Registry.projectiles.getOrDefault(key, new ProjectileDef());
    }

    public static AttackPattern get(String key) {
        return patterns.get(key);
    }
}
