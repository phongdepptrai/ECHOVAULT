package com.echovault.bosses;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.echovault.Player;
import com.echovault.RoomManager;
import com.echovault.enemies.Swarmer;
import com.echovault.utils.Telegraph;
import com.echovault.pattern.PatternFactory;

public class BellwetherBoss extends Boss {

    public BellwetherBoss(float x, float y) {
        super(x, y, 50, 400, "The Bellwether");
        this.color = Color.TEAL;
    }

    @Override
    public void initPhases() {
        // Phase 1: Swarmers
        phases.add(new Phase(1.0f) {
            float spawnTimer = 0;
            @Override public void onEnter(Boss boss, RoomManager rm) {
                 // Initial swarm
                 spawnSwarm(boss, rm);
            }
            @Override public void onExit(Boss boss, RoomManager rm) {}

            @Override
            public void update(float delta, Boss boss, Player p, RoomManager rm) {
                spawnTimer += delta;
                if (spawnTimer > 4.0f) {
                    spawnSwarm(boss, rm);
                    spawnTimer = 0;
                }

                // Slow aimed shots
                if (MathUtils.random() < 0.05f) {
                    PatternFactory.get("fan_5").execute(rm, boss.position, p.position);
                }
            }

            private void spawnSwarm(Boss boss, RoomManager rm) {
                for(int i=0; i<4; i++) {
                    rm.addEnemy(new Swarmer(boss.position.x + MathUtils.random(-50,50), boss.position.y + MathUtils.random(-50,50)));
                }
            }
        });

        // Phase 2: Bomb Grid
        phases.add(new Phase(0.7f) {
            float bombTimer = 0;
            @Override public void onEnter(Boss boss, RoomManager rm) {}
            @Override public void onExit(Boss boss, RoomManager rm) {}
            @Override
            public void update(float delta, Boss boss, Player p, RoomManager rm) {
                bombTimer += delta;
                if (bombTimer > 2.0f) {
                     // 3 Random bombs
                     for(int i=0; i<3; i++) {
                         Vector2 pos = new Vector2(MathUtils.random(100, 1180), MathUtils.random(100, 620));
                         // Just visual telegraphs that hurt?
                         // We need an entity for delay. Or just use a dummy logic in Boss?
                         // Ideally "Bomber" enemy logic or just spawn a temporary "ExplosionHazard".
                         // For MVP, lets spawn "Bomber" enemies that explode instantly? No.
                         // Let's assume RoomManager handles telegraphs? No.
                         // Let's spawn a "StationaryBomber" (reusing Bomber class but 0 speed).
                         // Or better: Bullet with 0 speed and large size?
                         // Let's use `activeTelegraph` but we can only have one.
                         // Simplification: Boss spawns a Bomber enemy.
                         // "Bomber: runs close, plants bomb".
                         // Boss throws bombs?
                         // Let's spawn "Bomber" enemies at the location.
                         // Actually "Bomber AoE: place 3 telegraphs".
                         // We need a way to create independent hazards.
                         // Let's spawn a "Mine" enemy (0 speed).
                         rm.addEnemy(new Swarmer(pos.x, pos.y) {
                             {
                                 this.color = Color.BLACK;
                                 this.radius = 40;
                                 this.hp = 1; // Die fast
                             }
                             // Hacky overrides for Mine behavior
                             float t = 0;
                             Telegraph tel;
                             @Override public void update(float d, Player pl, RoomManager r) {
                                 t += d;
                                 if (tel == null) tel = new Telegraph(Telegraph.Type.CIRCLE, position, null, 60, 1.5f, Color.ORANGE);
                                 if (tel.update(d)) {
                                     if (pl.position.dst(position) < 60) pl.takeDamage(2);
                                     dead = true;
                                 }
                             }
                             @Override public void renderTelegraph(com.badlogic.gdx.graphics.glutils.ShapeRenderer sr) {
                                 if (tel!=null) tel.render(sr);
                             }
                         });
                     }
                     bombTimer = 0;
                }
            }
        });

        // Phase 3: Laser Dash
        phases.add(new Phase(0.4f) {
             float timer = 0;
             boolean dashing = false;
             Vector2 dashEnd = new Vector2();

             @Override public void onEnter(Boss boss, RoomManager rm) {}
             @Override public void onExit(Boss boss, RoomManager rm) {}
             @Override
             public void update(float delta, Boss boss, Player p, RoomManager rm) {
                 timer += delta;

                 if (boss.activeTelegraph != null) {
                     if (boss.activeTelegraph.update(delta)) {
                         // Dash & Laser back
                         boss.position.set(dashEnd);
                         // Fire laser back to start?
                         // "Laser + dash combo"
                         boss.activeTelegraph = null;
                         // Fire ring
                         PatternFactory.get("ring_12").execute(rm, boss.position, null);
                         timer = 0;
                     }
                 } else if (timer > 2.0f) {
                     // Setup Dash
                     dashEnd.set(p.position);
                     boss.activeTelegraph = new Telegraph(Telegraph.Type.LINE, boss.position, dashEnd, 40f, 0.8f, Color.CYAN);
                 }
             }
        });
    }
}
