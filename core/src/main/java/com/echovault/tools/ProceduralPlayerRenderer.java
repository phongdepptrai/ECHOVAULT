package com.echovault.tools;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.MathUtils;

public class ProceduralPlayerRenderer {

    // Colors
    private static final int COLOR_BODY = Color.rgba8888(0.2f, 0.4f, 0.8f, 1f);
    private static final int COLOR_HEAD = Color.rgba8888(0.9f, 0.8f, 0.7f, 1f);
    private static final int COLOR_LIMB = Color.rgba8888(0.1f, 0.3f, 0.7f, 1f);
    private static final int COLOR_WEAPON = Color.rgba8888(0.3f, 0.3f, 0.3f, 1f);
    private static final int COLOR_SHADOW = Color.rgba8888(0f, 0f, 0f, 0.3f);
    private static final int COLOR_WHITE = Color.rgba8888(1f, 1f, 1f, 1f);

    // Config
    private int frameSize;

    public ProceduralPlayerRenderer(int frameSize) {
        this.frameSize = frameSize;
    }

    public Pixmap renderFrame(AnimationType anim, Direction dir, int frameIndex, long seed) {
        MathUtils.random.setSeed(seed); // Deterministic RNG if needed (e.g. for death particles)

        Pixmap p = new Pixmap(frameSize, frameSize, Pixmap.Format.RGBA8888);
        p.setColor(0);
        p.fill(); // Transparent

        int cx = frameSize / 2;
        int cy = frameSize / 2;
        int scale = frameSize / 32; // Base design on ~32px logic
        if (scale < 1) scale = 1;

        AnimationSpec.AnimDef def = AnimationSpec.get(anim);
        float progress = (float)frameIndex / (float)def.frameCount;

        // --- Logic Vars ---
        float bobY = 0;
        float armSwing = 0;
        float legSwing = 0;
        float recoil = 0;
        float dashStretchX = 1f;
        float dashStretchY = 1f;
        float alpha = 1f;
        boolean flash = false;

        // --- Animation Logic ---
        switch (anim) {
            case IDLE:
                // Breathe/Bob
                bobY = MathUtils.sin(progress * MathUtils.PI * 2) * 1f * scale;
                break;
            case WALK:
                // Bob + Swing
                bobY = Math.abs(MathUtils.sin(progress * MathUtils.PI * 2)) * 2f * scale;
                armSwing = MathUtils.cos(progress * MathUtils.PI * 2) * 3f * scale;
                legSwing = MathUtils.sin(progress * MathUtils.PI * 2) * 3f * scale;
                break;
            case SHOOT:
                // Recoil
                if (frameIndex < 2) recoil = 4f * scale;
                else recoil = MathUtils.lerp(4f * scale, 0, (frameIndex - 2) / 4f);
                break;
            case DASH:
                if (dir == Direction.LEFT || dir == Direction.RIGHT) {
                    dashStretchX = 1.4f;
                    dashStretchY = 0.6f;
                } else {
                    dashStretchX = 0.6f;
                    dashStretchY = 1.4f;
                }
                // Motion streak could be drawn behind
                break;
            case HURT:
                if (frameIndex % 2 == 0) flash = true;
                break;
            case DEATH:
                alpha = 1f - progress;
                // Fall down effect?
                break;
        }

        // --- Drawing ---

        // Shadow
        p.setColor(COLOR_SHADOW);
        p.fillCircle(cx, (int)(cy - 12 * scale), (int)(6 * scale * dashStretchX));

        if (anim == AnimationType.DEATH && alpha < 0.1f) return p;

        // Apply Transform Logic: we draw primitive shapes offset by state

        // Draw Order depends on Direction
        // UP: Weapon(behind) -> Legs -> Body -> Head -> Weapon(front?) -> Arms
        // DOWN: Weapon(front?) -> Legs -> Body -> Head -> Arms
        // LEFT/RIGHT: Right Arm -> Right Leg -> Body -> Left Leg -> Left Arm

        // Simplified Painter's Algorithm

        int bodyW = 8 * scale;
        int bodyH = 10 * scale;
        int headR = 5 * scale;

        int baseX = cx;
        int baseY = (int)(cy + bobY);

        // Legs
        int legW = 3 * scale;
        int legH = 6 * scale;
        int legOffX = 2 * scale;
        int legOffY = 6 * scale; // Below body center

        // Color override for Flash
        int cBody = flash ? COLOR_WHITE : COLOR_BODY;
        int cHead = flash ? COLOR_WHITE : COLOR_HEAD;
        int cLimb = flash ? COLOR_WHITE : COLOR_LIMB;
        int cWeap = flash ? COLOR_WHITE : COLOR_WEAPON;

        if (anim == AnimationType.DEATH) {
            // Crumble/Scatter
            // Random rects
            p.setColor(cBody);
            for (int i=0; i<10; i++) {
                int rx = MathUtils.random(-10, 10) * scale;
                int ry = MathUtils.random(-10, 10) * scale - (int)(progress * 10 * scale);
                p.fillRectangle(cx + rx, cy + ry, scale*2, scale*2);
            }
            return p;
        }

        // --- Helper for drawing rotated/scaled rects? Pixmap doesn't support rotation.
        // We stick to axis aligned for simplicity as requested (Basic shapes).

        // Draw Left Leg (Back)
        if (dir == Direction.RIGHT || dir == Direction.UP || dir == Direction.DOWN || dir == Direction.LEFT) {
             drawRect(p, baseX - legOffX - legW/2, baseY - legOffY - (int)legSwing, legW, legH, cLimb);
        }

        // Draw Right Leg (Back/Front)
        if (dir == Direction.RIGHT || dir == Direction.UP || dir == Direction.DOWN || dir == Direction.LEFT) {
             drawRect(p, baseX + legOffX - legW/2, baseY - legOffY + (int)legSwing, legW, legH, cLimb);
        }

        // Draw Body
        drawRect(p, baseX - bodyW/2, baseY - bodyH/2, bodyW, (int)(bodyH * dashStretchY), cBody);

        // Draw Head
        int headY = baseY + bodyH/2 + headR/2;
        p.setColor(cHead);
        p.fillCircle(baseX, headY, headR);

        // Face/Eyes
        p.setColor(Color.BLACK);
        int eyeOffX = 2 * scale;
        int eyeOffY = 1 * scale;
        if (dir == Direction.DOWN) {
            p.fillRectangle(baseX - eyeOffX, headY + eyeOffY, scale, scale);
            p.fillRectangle(baseX + eyeOffX - scale, headY + eyeOffY, scale, scale);
        } else if (dir == Direction.RIGHT) {
            p.fillRectangle(baseX + eyeOffX, headY + eyeOffY, scale, scale);
        } else if (dir == Direction.LEFT) {
             p.fillRectangle(baseX - eyeOffX - scale, headY + eyeOffY, scale, scale);
        }

        // Weapon
        int weapW = 4 * scale;
        int weapH = 8 * scale;
        int weapX = baseX;
        int weapY = baseY;

        // Weapon Logic
        if (dir == Direction.DOWN) {
            weapX = baseX + 4 * scale;
            weapY = baseY - 2 * scale;
            if (anim == AnimationType.SHOOT) weapY -= recoil;
            drawRect(p, weapX, weapY, 2*scale, 6*scale, cWeap);
        } else if (dir == Direction.UP) {
             // Behind body, drawn first?
             // Too late, already drew body. Overdraw or check z-order?
             // Simple hack: don't draw weapon for UP if simple.
             // Or draw extended to side.
             weapX = baseX + 4 * scale;
             weapY = baseY;
             if (anim == AnimationType.SHOOT) weapY += recoil;
             drawRect(p, weapX, weapY, 2*scale, 6*scale, cWeap);
        } else if (dir == Direction.RIGHT) {
            weapX = baseX + 4 * scale;
             if (anim == AnimationType.SHOOT) weapX -= recoil;
            drawRect(p, weapX, baseY, 8*scale, 2*scale, cWeap);
        } else if (dir == Direction.LEFT) {
             weapX = baseX - 10 * scale;
             if (anim == AnimationType.SHOOT) weapX += recoil;
             drawRect(p, weapX, baseY, 8*scale, 2*scale, cWeap);
        }

        // Draw Arms (Simple rects)
        // ...

        return p;
    }

    private void drawRect(Pixmap p, int x, int y, int w, int h, int color) {
        // y is up in LibGDX world? No, Pixmap 0,0 is Top-Left.
        // My logic above assumed Y goes UP (Game coordinates).
        // I need to flip Y.
        // cy is center.
        // Screen Y = frameSize - Game Y.

        int py = frameSize - y - h;
        p.setColor(color);
        p.fillRectangle(x, py, w, h);
    }
}
