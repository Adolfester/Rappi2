package io.github.rappi;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class MainMenu extends ScreenAdapter {

    private final Main game;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    private Rectangle botonJugar;
    private Vector3 touchPoint;

    private float anchoBoton = 200;
    private float altoBoton = 60;
    private OrthographicCamera camera;
    private Viewport viewport;

    public MainMenu(Main game) {
        this.game = game;
    }

    @Override
    public void show() {

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();

        touchPoint = new Vector3();

        botonJugar = new Rectangle();

        actualizarBoton();
    }

    private void actualizarBoton() {

        float posX = (Gdx.graphics.getWidth() - anchoBoton) / 2f;
        float posY = (Gdx.graphics.getHeight() - altoBoton) / 2f;

        botonJugar.set(
            posX,
            posY,
            anchoBoton,
            altoBoton
        );
    }

    @Override
    public void render(float delta) {

        // --- INTERACCIÓN ---

        if (Gdx.input.justTouched()) {

            touchPoint.set(
                Gdx.input.getX(),
                Gdx.input.getY(),
                0
            );

            // Convertir coordenadas de pantalla
            touchPoint.y = Gdx.graphics.getHeight() - touchPoint.y;

            if (botonJugar.contains(touchPoint.x, touchPoint.y)) {

                game.setScreen(new GameScreen(game));

                dispose();

                return;
            }
        }

        // --- RENDERIZADO ---

        Gdx.gl.glClearColor(
            0.1f,
            0.1f,
            0.15f,
            1
        );

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Botón
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(
            0.2f,
            0.7f,
            0.3f,
            1
        );

        shapeRenderer.rect(
            botonJugar.x,
            botonJugar.y,
            botonJugar.width,
            botonJugar.height
        );

        shapeRenderer.end();

        // Texto
        batch.begin();

        font.draw(
            batch,
            "JUGAR",
            botonJugar.x + 75,
            botonJugar.y + 35
        );

        batch.end();
    }

    @Override
    public void resize(int width, int height) {

        actualizarBoton();
    }

    @Override
    public void dispose() {

        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
    }


}
