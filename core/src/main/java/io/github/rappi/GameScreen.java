package io.github.rappi;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class GameScreen implements Screen {

    private final Main game;

    private SpriteBatch batch;
    private Player player;

    public GameScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {

        batch = new SpriteBatch();

        player = new Player();
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(0.5f, 0.8f, 1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        player.update(delta);

        batch.begin();

        player.draw(batch);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

        batch.dispose();
        player.dispose();

    }
}
