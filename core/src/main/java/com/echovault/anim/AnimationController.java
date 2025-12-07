package com.echovault.anim;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class AnimationController {
    public AnimatedSprite sprite;
    public AnimationSet currentSet;

    public String currentState = "idle";
    public String currentDir = "down";
    public boolean lockState = false;

    public AnimationController(AnimationSet set) {
        this.currentSet = set;
        this.sprite = new AnimatedSprite();
        updateClip();
    }

    public void update(float delta) {
        sprite.update(delta);
        if (lockState && sprite.isFinished()) {
            lockState = false;
            setState("idle", currentDir, true);
        }
    }

    public void setState(String state, String dir, boolean loop) {
        if (lockState) return;

        if (!state.equals(currentState) || !dir.equals(currentDir)) {
            currentState = state;
            currentDir = dir;
            updateClip();
            if (!loop) lockState = true;
        }
    }

    // Force set state ignoring lock
    public void forceState(String state, String dir, boolean loop) {
        lockState = false;
        setState(state, dir, loop);
    }

    private void updateClip() {
        if (currentSet != null) {
            sprite.setClip(currentSet.getClip(currentState, currentDir));
        }
    }

    public TextureRegion getRegion() {
        return sprite.getFrame();
    }
}
