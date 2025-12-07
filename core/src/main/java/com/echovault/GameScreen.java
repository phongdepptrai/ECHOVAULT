package com.echovault;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class GameScreen implements Screen {
    final EchovaultGame game;
    OrthographicCamera camera;

    // Fixed timestep logic
    private float accumulator = 0;
    private static final float STEP = 1/60f;

    private RoomManager roomManager;
    private Player player;
    private boolean gameOver = false;
    private boolean debugMode = false;

    public GameScreen(final EchovaultGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1280, 720);

        init();
    }

    private void init() {
        // Initialize systems
        com.echovault.assets.AssetGenerator.init();
        com.echovault.combat.projectile.ProjectileFactory.init();
        com.echovault.pattern.PatternFactory.init();
        com.echovault.core.Registry.initDefaults();

        player = new Player(640, 360);
        roomManager = new RoomManager(player);
        gameOver = false;
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.F1)) {
            debugMode = !debugMode;
        }

        if (gameOver) {
            if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.R)) {
                init();
            }
        } else {
             accumulator += delta;
            while (accumulator >= STEP) {
                update(STEP);
                accumulator -= STEP;
            }
        }

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        game.shapeRenderer.setProjectionMatrix(camera.combined);

        drawWorld();
        drawUI();
    }

    private void update(float delta) {
        if (player.dead) {
            gameOver = true;
            return;
        }

        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mousePos);
        InputFrame input = InputManager.getCurrentInput(player.position, new Vector2(mousePos.x, mousePos.y));

        player.update(delta, input, roomManager);
        roomManager.update(delta);
    }

    private void drawWorld() {
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Render Telegraphs (Underneath entities)
        for (Entity e : roomManager.entities) {
            e.renderTelegraph(game.shapeRenderer);
        }

        // Draw Walls
        Rectangle b = roomManager.bounds;
        game.shapeRenderer.setColor(Color.DARK_GRAY);
        // Draw 4 walls
        game.shapeRenderer.rect(0, 0, 1280, b.y); // Bottom
        game.shapeRenderer.rect(0, b.y + b.height, 1280, 720 - (b.y + b.height)); // Top
        game.shapeRenderer.rect(0, b.y, b.x, b.height); // Left
        game.shapeRenderer.rect(b.x + b.width, b.y, 1280 - (b.x + b.width), b.height); // Right

        // Draw Doors (Simple gaps or colored rects)
        game.shapeRenderer.setColor(Color.BLACK);
        if (roomManager.roomCleared) {
            // "Open" doors (just draw over walls? or just gaps? logic allows passing)
            game.shapeRenderer.setColor(Color.GREEN);
        } else {
             game.shapeRenderer.setColor(Color.RED);
        }

        // Draw visual doors
        float doorSize = 100;
        game.shapeRenderer.rect(b.x + b.width / 2 - doorSize/2, b.y - 10, doorSize, 10); // Bottom
        game.shapeRenderer.rect(b.x + b.width / 2 - doorSize/2, b.y + b.height, doorSize, 10); // Top
        game.shapeRenderer.rect(b.x - 10, b.y + b.height/2 - doorSize/2, 10, doorSize); // Left
        game.shapeRenderer.rect(b.x + b.width, b.y + b.height/2 - doorSize/2, 10, doorSize); // Right


        // Draw Entities (ShapeRenderer Fallback)
        for (Entity e : roomManager.entities) {
            e.render(game.shapeRenderer);
        }

        player.render(game.shapeRenderer);

        for (Bullet bullet : roomManager.bullets) {
            bullet.render(game.shapeRenderer);
        }

        game.shapeRenderer.end();

        // SPRITE BATCH PASS
        game.batch.begin();

        for (Entity e : roomManager.entities) {
            e.render(game.batch);
        }

        player.render(game.batch);

        for (Bullet bullet : roomManager.bullets) {
             if (bullet instanceof com.echovault.combat.projectile.ProjectileEntity) {
                 ((com.echovault.combat.projectile.ProjectileEntity)bullet).render(game.batch);
             }
        }

        game.batch.end();

        // Resume ShapeRenderer for items/debug
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Draw Items
        for (Item item : roomManager.activeItems) {
            item.render(game.shapeRenderer);
        }

        game.shapeRenderer.end();

        if (debugMode) {
            game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            game.shapeRenderer.setColor(Color.WHITE);
            for (Entity e : roomManager.entities) {
                 game.shapeRenderer.rect(e.getBounds().x, e.getBounds().y, e.getBounds().width, e.getBounds().height);
            }
            game.shapeRenderer.rect(player.getBounds().x, player.getBounds().y, player.getBounds().width, player.getBounds().height);
            game.shapeRenderer.end();
        }
    }

    private void drawUI() {
        game.batch.begin();
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "HP: " + (int)player.hp + "/" + (int)player.maxHp, 20, 700);
        game.font.draw(game.batch, "Room: " + roomManager.roomIndex, 20, 680);
        game.font.draw(game.batch, "Items: " + player.items.size(), 20, 660);

        // BOSS BAR
        for (Entity e : roomManager.entities) {
            if (e instanceof com.echovault.bosses.Boss) {
                com.echovault.bosses.Boss boss = (com.echovault.bosses.Boss)e;
                game.font.setColor(Color.RED);
                game.font.draw(game.batch, boss.getName(), 580, 700);
                // Simple bar
                game.batch.end();
                game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                game.shapeRenderer.setColor(Color.BLACK);
                game.shapeRenderer.rect(400, 680, 480, 10);
                game.shapeRenderer.setColor(Color.RED);
                game.shapeRenderer.rect(400, 680, 480 * (boss.hp / boss.maxHp), 10);
                game.shapeRenderer.end();
                game.batch.begin();
                break; // Only 1 boss bar
            }
        }

        if (roomManager.roomCleared && !roomManager.itemPhase) {
            game.font.draw(game.batch, "CLEAR! Go to next room", 600, 400);
        }
        if (roomManager.itemPhase) {
             game.font.draw(game.batch, "PICK AN ITEM", 600, 450);
             // Draw Item names
             for (Item item : roomManager.activeItems) {
                 game.font.draw(game.batch, item.type.toString(), item.x - 20, item.y + 30);
             }
        }

        if (gameOver) {
            game.font.setColor(Color.RED);
            game.font.getData().setScale(2);
            game.font.draw(game.batch, "GAME OVER", 550, 400);
            game.font.getData().setScale(1);
            game.font.draw(game.batch, "Press R to Restart", 580, 350);
        }

        if (debugMode) {
            game.font.setColor(Color.YELLOW);
            game.font.draw(game.batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 20, 20);
            game.font.draw(game.batch, "Entities: " + roomManager.entities.size(), 20, 40);
            game.font.draw(game.batch, "Bullets: " + roomManager.bullets.size(), 20, 60);

            // Draw Enemy Names above heads
            for (Entity e : roomManager.entities) {
                 game.font.draw(game.batch, e.getClass().getSimpleName(), e.position.x - 20, e.position.y + 40);
            }
        }

        game.batch.end();
    }

    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {}
}
