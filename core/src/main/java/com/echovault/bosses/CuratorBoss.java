package com.echovault.bosses;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.echovault.Ghost;
import com.echovault.Player;
import com.echovault.RoomManager;
import com.echovault.enemies.Turret;
import com.echovault.utils.BulletPatterns;
import com.echovault.utils.Telegraph;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class CuratorBoss extends Boss {

    public CuratorBoss(float x, float y) {
        super(x, y, 40, 300, "The Curator"); // 300 HP
    }

    @Override
    public void initPhases() {
        // Phase 1: 100-66%
        phases.add(new Phase(1.0f) {
            float timer = 0;
            boolean dashReady = false;
            Telegraph dashTell;
            Vector2 dashTarget = new Vector2();

            @Override public void onEnter(Boss boss, RoomManager rm) {}
            @Override public void onExit(Boss boss, RoomManager rm) {}

            @Override
            public void update(float delta, Boss boss, Player p, RoomManager rm) {
                timer += delta;

                if (boss.activeTelegraph != null) {
                    if (boss.activeTelegraph.update(delta)) {
                        // Dash
                        boss.position.set(dashTarget);
                        boss.activeTelegraph = null;
                        timer = 0;
                        BulletPatterns.shootRing(rm, boss.position, 12, 150f, 1, 1, 0);
                    }
                    return;
                }

                // Pattern Logic
                if (timer > 2.0f) {
                     float rnd = MathUtils.random();
                     if (rnd < 0.3f) {
                         // Setup Dash
                         Vector2 dest = new Vector2(MathUtils.random(200, 1000), MathUtils.random(200, 500));
                         dashTarget.set(dest);
                         boss.activeTelegraph = new Telegraph(Telegraph.Type.LINE, boss.position, dest, boss.radius*2, 0.6f, Color.ORANGE);
                     } else {
                         // Shoot Fan + Aimed
                         BulletPatterns.shootFan(rm, boss.position, p.position, 5, 45f, 200f, 1, 1);
                         BulletPatterns.shootAimed(rm, boss.position, p.position, 300f, 1, 1);
                         timer = 0.5f; // Faster cycle
                     }
                }
            }
        });

        // Phase 2: 66-33%
        phases.add(new Phase(0.66f) {
            float timer = 0;
            int volleyCount = 0;

            @Override
            public void onEnter(Boss boss, RoomManager rm) {
                // Spawn 2 Turrets
                rm.addEnemy(new Turret(200, 500));
                rm.addEnemy(new Turret(1000, 500));
            }
            @Override public void onExit(Boss boss, RoomManager rm) {}

            @Override
            public void update(float delta, Boss boss, Player p, RoomManager rm) {
                timer += delta;
                if (timer > 1.5f) {
                    BulletPatterns.shootRing(rm, boss.position, 16, 180f, 1, 1, volleyCount * 10f);
                    volleyCount++;
                    timer = 0;
                }
            }
        });

        // Phase 3: 33-0%
        phases.add(new Phase(0.33f) {
            float timer = 0;
            boolean ghostSpawned = false;

            @Override public void onEnter(Boss boss, RoomManager rm) {
                 // Echo Surge visual?
            }
            @Override public void onExit(Boss boss, RoomManager rm) {}

            @Override
            public void update(float delta, Boss boss, Player p, RoomManager rm) {
                timer += delta;

                if (!ghostSpawned && timer > 1.0f) {
                    // Spawn Ghost Replay
                    if (p.inputHistory.size() > 60) {
                         // Use recent history
                         Ghost g = new Ghost(boss.position.x, boss.position.y, p.inputHistory, false, false, false);
                         // Make it visible/distinct
                         g.color = Color.BLACK;
                         rm.addEnemy(g);
                    }
                    ghostSpawned = true;
                }

                // Spiral
                if (timer > 0.1f) {
                     BulletPatterns.shootSpiral(rm, boss.position, 1, (boss.stateTimer * 100f) % 360, 200f, 1, 1);
                     // timer not reset, continuous
                }

                if (timer > 4.0f) {
                    // Burst Ring
                    BulletPatterns.shootRing(rm, boss.position, 20, 300f, 1, 1, 0);
                    timer = 1.5f; // Reset loop partly
                }
            }
        });
    }

    @Override
    public void renderTelegraph(ShapeRenderer sr) {
        // Need to expose active telegraph from phases?
        // Hack: Store 'activeTelegraph' in Boss class or make phases renderable.
        // For MVP, Phase 1 logic uses local var. We can't reach it easily.
        // Let's rely on standard Entity logic.
        // But Phase 1 dashTell is local.
        // Better: Boss has `currentTelegraph` field.
    }

    // Quick fix: Add activeTelegraph to Boss base
    public Telegraph activeTelegraph; // Use this in phases

    @Override
    public void render(ShapeRenderer sr) {
        if (activeTelegraph != null) activeTelegraph.render(sr);
        super.render(sr);
    }
}
