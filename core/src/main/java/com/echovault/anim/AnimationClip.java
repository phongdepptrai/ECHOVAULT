package com.echovault.anim;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class AnimationClip {
    public TextureRegion[] frames;
    public float frameDuration;
    public boolean looping;

    public AnimationClip(float frameDuration, boolean looping, TextureRegion... frames) {
        this.frameDuration = frameDuration;
        this.looping = looping;
        this.frames = frames;
    }
}
