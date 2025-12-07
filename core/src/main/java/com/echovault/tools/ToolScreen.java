package com.echovault.tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class ToolScreen implements Screen {
    private Stage stage;
    private Skin skin;
    private ProceduralPlayerRenderer renderer;
    private Image previewImage;
    private Texture checkerBg;

    // State
    private AnimationType currentAnim = AnimationType.IDLE;
    private Direction currentDir = Direction.DOWN;
    private int frameIndex = 0;
    private boolean playing = true;
    private float timer = 0;

    public ToolScreen() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = UiUtils.createBasicSkin();
        renderer = new ProceduralPlayerRenderer(128); // Preview size 128

        setupUI();
        createCheckerboard();
    }

    private void createCheckerboard() {
        Pixmap p = new Pixmap(20, 20, Pixmap.Format.RGBA8888);
        p.setColor(Color.LIGHT_GRAY);
        p.fill();
        p.setColor(Color.GRAY);
        p.fillRectangle(0, 0, 10, 10);
        p.fillRectangle(10, 10, 10, 10);
        checkerBg = new Texture(p);
        checkerBg.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
    }

    private void setupUI() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // Left Panel: Controls
        Table controls = new Table();
        root.add(controls).width(300).fillY().pad(10);

        // Anim Select
        controls.add(new Label("Animation:", skin)).left();
        final SelectBox<AnimationType> animBox = new SelectBox<>(skin);
        animBox.setItems(AnimationType.values());
        animBox.setSelected(currentAnim);
        animBox.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                currentAnim = animBox.getSelected();
                frameIndex = 0;
            }
        });
        controls.add(animBox).fillX().row();

        // Dir Select
        controls.add(new Label("Direction:", skin)).left();
        final SelectBox<Direction> dirBox = new SelectBox<>(skin);
        dirBox.setItems(Direction.values());
        dirBox.setSelected(currentDir);
        dirBox.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                currentDir = dirBox.getSelected();
            }
        });
        controls.add(dirBox).fillX().row();

        // Scrub
        controls.add(new Label("Frame:", skin)).left();
        final Slider scrub = new Slider(0, 10, 1, false, skin);
        controls.add(scrub).fillX().row();

        // Play/Pause
        final TextButton playBtn = new TextButton("Pause", skin);
        playBtn.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                playing = !playing;
                playBtn.setText(playing ? "Pause" : "Play");
            }
        });
        controls.add(playBtn).colspan(2).fillX().padTop(10).row();

        // Export
        TextButton exportBtn = new TextButton("Export All", skin);
        exportBtn.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                Exporter.export(Gdx.files.local("export"), 64, true, null, null, 12345);
                System.out.println("Exported!");
            }
        });
        controls.add(exportBtn).colspan(2).fillX().padTop(20).row();

        // Right Panel: Preview
        Table preview = new Table();
        root.add(preview).expand().fill();

        previewImage = new Image();
        preview.add(previewImage).size(256, 256);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update Logic
        AnimationSpec.AnimDef def = AnimationSpec.get(currentAnim);

        if (playing) {
            timer += delta;
            if (timer >= 1f / def.fps) {
                timer -= 1f / def.fps;
                frameIndex = (frameIndex + 1) % def.frameCount;
            }
        }

        // Render Preview
        Pixmap p = renderer.renderFrame(currentAnim, currentDir, frameIndex, 12345);
        Texture t = new Texture(p);
        p.dispose();

        // Use stack to draw checkerboard behind?
        // For simple Image widget, we can just draw checkerboard first.

        stage.getBatch().begin();
        stage.getBatch().draw(checkerBg, previewImage.getX() + previewImage.getParent().getX(), previewImage.getY() + previewImage.getParent().getY(), 256, 256, 0, 0, 12.8f, 12.8f);
        stage.getBatch().end();

        Drawable d = new TextureRegionDrawable(t);
        previewImage.setDrawable(d);

        stage.act(delta);
        stage.draw();

        // Cleanup texture to avoid leak?
        // Ideally we reuse a texture or dispose old one.
        // For tool, it's okay, but better to dispose previous frame's texture.
        // Since we create new Texture every frame here, garbage collection might struggle.
        // Let's dispose the OLD drawable's texture if we can access it.
        // But TextureRegionDrawable holds Region holds Texture.
        // Optimization: Keep one texture and use `drawPixmap`.
        // But Texture needs to be RGBA.
        if (currentTexture != null) {
            currentTexture.dispose();
        }
        currentTexture = t;
    }

    private Texture currentTexture;

    @Override public void show() {}
    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { stage.dispose(); if(currentTexture!=null) currentTexture.dispose(); }
}
