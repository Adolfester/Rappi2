package io.github.rappi;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MainMenu implements Screen {

    private final Main game;

    private Stage stage;
    private Skin skin;

    public MainMenu(Main game) {
        this.game = game;
    }

    @Override
    public void show() {

        stage = new Stage(new ScreenViewport());

        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("uiskin.json"));

        Table table = new Table();

        table.setFillParent(true);

        stage.addActor(table);

        Label titulo = new Label(
            "DINO RUNNER",
            skin
        );

        TextButton botonJugar = new TextButton(
            "JUGAR",
            skin
        );

        TextButton botonConfiguracion = new TextButton(
            "CONFIGURACIÓN",
            skin
        );

        table.add(titulo).padBottom(40);
        table.row();

        table.add(botonJugar)
            .width(250)
            .height(60)
            .padBottom(20);

        table.row();

        table.add(botonConfiguracion)
            .width(250)
            .height(60);

        botonJugar.addListener(
            new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {

                @Override
                public void clicked(
                    com.badlogic.gdx.scenes.scene2d.InputEvent event,
                    float x,
                    float y
                ) {

                    game.setScreen(
                        new GameScreen(game)
                    );
                }
            }
        );
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(
            0.1f,
            0.15f,
            0.2f,
            1
        );

        Gdx.gl.glClear(
            GL20.GL_COLOR_BUFFER_BIT
        );

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {

        stage.getViewport().update(
            width,
            height,
            true
        );
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

        stage.dispose();
        skin.dispose();
    }
}
