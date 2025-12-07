package com.echovault;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;

public class InputManager {

    public static InputFrame getCurrentInput(Vector2 playerPos, Vector2 mouseWorldPos) {
        InputFrame frame = new InputFrame();

        frame.up = Gdx.input.isKeyPressed(Input.Keys.W);
        frame.down = Gdx.input.isKeyPressed(Input.Keys.S);
        frame.left = Gdx.input.isKeyPressed(Input.Keys.A);
        frame.right = Gdx.input.isKeyPressed(Input.Keys.D);

        frame.shoot = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        // Dash is right click
        frame.dash = Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT);
        // Note: isButtonJustPressed is frame-dependent, but since we sample at 60Hz logic tick,
        // we might miss it if logic tick < render fps?
        // Ideally we should process input events.
        // For MVP 60fps locked, polling is okay.
        // Better: check if it was pressed *since last tick*.
        // But Gdx.input.isButtonJustPressed() resets every frame.
        // If we run logic multiple times per frame, we might double-consume.
        // If we run logic less than FPS, we might miss.
        // Let's rely on Gdx.input.isButtonPressed(RIGHT) and handle "just pressed" logic in the entity update
        // by storing previous state, OR ensure we just use isButtonPressed and cooldown logic handles it.
        // The prompt says "Right click dash with cooldown".
        // Let's store "isDown" and let player logic handle the trigger on rising edge.
        frame.dash = Gdx.input.isButtonPressed(Input.Buttons.RIGHT);

        Vector2 diff = new Vector2(mouseWorldPos).sub(playerPos);
        frame.aimAngle = diff.angleDeg();

        return frame;
    }
}
