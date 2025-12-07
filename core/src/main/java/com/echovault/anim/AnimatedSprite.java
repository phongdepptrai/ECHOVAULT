package com.echovault.anim;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class AnimatedSprite {
    private AnimationClip currentClip;
    private float stateTime;

    public void setClip(AnimationClip clip) {
        if (this.currentClip != clip) {
            this.currentClip = clip;
            this.stateTime = 0;
        }
    }

    public void update(float delta) {
        stateTime += delta;
    }

    public TextureRegion getFrame() {
        if (currentClip == null) return null;
        int frameIndex = (int)(stateTime / currentClip.frameDuration);

        if (currentClip.looping) {
            frameIndex = frameIndex % currentClip.frames.length;
        } else {
            frameIndex = Math.min(frameIndex, currentClip.frames.length - 1);
        }

        return currentClip.frames[frameIndex];
    }

    public boolean isFinished() {
        if (currentClip == null || currentClip.looping) return false;
        return stateTime >= currentClip.frameDuration * currentClip.frames.length;
    }
}
