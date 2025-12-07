package com.echovault.assets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.echovault.anim.AnimationBank;
import com.echovault.anim.AnimationClip;
import com.echovault.anim.AnimationSet;

public class AssetGenerator {

    // We store Textures to dispose later if needed
    public static Texture playerSheet;
    public static Texture enemySheet;
    public static Texture projectileSheet;

    public static void init() {
        generatePlayerAssets();
        generateEnemyAssets();
        generateProjectileAssets();
    }

    private static void generatePlayerAssets() {
        int size = 32;
        int frames = 4;
        Pixmap p = new Pixmap(size * frames, size, Pixmap.Format.RGBA8888);
        p.setColor(Color.WHITE); // Player is white/tinted

        // Idle/Move frames (simple bobbing)
        for (int i=0; i<frames; i++) {
            p.fillCircle(i*size + size/2, size/2, size/3);
            // Eyes
            p.setColor(Color.BLACK);
            p.fillRectangle(i*size + size/2 + 2, size/2 + 2, 4, 4);
            p.fillRectangle(i*size + size/2 - 6, size/2 + 2, 4, 4);
            p.setColor(Color.WHITE);
        }

        playerSheet = new Texture(p);
        p.dispose();

        TextureRegion[][] tmp = TextureRegion.split(playerSheet, size, size);
        AnimationSet set = new AnimationSet();
        set.addClip("idle", "down", new AnimationClip(0.2f, true, tmp[0]));
        set.addClip("move", "down", new AnimationClip(0.1f, true, tmp[0])); // Reuse
        AnimationBank.addSet("player", set);
    }

    private static void generateEnemyAssets() {
        // Generic enemy blob
        int size = 32;
        Pixmap p = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        p.setColor(Color.WHITE); // Tinted by entity color
        p.fillRectangle(4, 4, 24, 24); // Box

        enemySheet = new Texture(p);
        p.dispose();

        TextureRegion reg = new TextureRegion(enemySheet);
        AnimationSet set = new AnimationSet();
        set.addClip("idle", "down", new AnimationClip(1f, true, reg));
        set.addClip("move", "down", new AnimationClip(1f, true, reg));

        // Add for all archetypes as fallback
        AnimationBank.addSet("enemy_generic", set);
    }

    private static void generateProjectileAssets() {
        int size = 16;
        Pixmap p = new Pixmap(size * 4, size, Pixmap.Format.RGBA8888);

        // 0: Circle (Normal)
        p.setColor(Color.WHITE);
        p.fillCircle(size/2, size/2, size/2 - 1);

        // 1: Diamond (Sharp)
        p.fillTriangle(size + size/2, 0, size + size, size/2, size, size/2);
        p.fillTriangle(size + size/2, size, size + size, size/2, size, size/2);

        // 2: Ring
        p.drawCircle(2*size + size/2, size/2, size/2 - 2);

        // 3: Beam glow
        p.setColor(1, 1, 1, 0.5f);
        p.fillCircle(3*size + size/2, size/2, size/2);

        projectileSheet = new Texture(p);
        p.dispose();

        // We will slice these in ProjectileVisual implementations
    }

    public static TextureRegion getProjectileRegion(int index) {
        return new TextureRegion(projectileSheet, index * 16, 0, 16, 16);
    }
}
