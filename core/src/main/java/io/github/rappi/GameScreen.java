package io.github.rappi;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public class GameScreen implements Screen {

    private final Main game;

    private SpriteBatch batch;
    private Player player;
    private Array<ParallaxLayer> backgroundLayers;
    private float gameSpeed = 300f;
    private Piso piso;

    // Gestor de puntaje y fuente TTF
    private ScoreManager scoreManager;
    private BitmapFont font;
    private GlyphLayout glyphLayout;

    public GameScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {

        batch = new SpriteBatch();
        piso = new Piso(40f, 4.0f);

        player = new Player(piso);
        backgroundLayers = new Array<>();

        backgroundLayers.add(new ParallaxLayer("1.png", 0.05f));   // Casi estático
        backgroundLayers.add(new ParallaxLayer("2.png", 0.15f));  // Muy lento
        backgroundLayers.add(new ParallaxLayer("3.png", 0.35f)); // Velocidad media
        backgroundLayers.add(new ParallaxLayer("4.png", 0.70f));  // Más rápido
        backgroundLayers.add(new ParallaxLayer("5.png", 0.80f));  // Más rápido

        // Inicializar puntaje
        scoreManager = new ScoreManager();

        // Cargar fuente pixelada usando archivo .fnt
        if (Gdx.files.internal("8bitOperatorPlus-Regular.fnt").exists()) {
            font = new BitmapFont(Gdx.files.internal("8bitOperatorPlus-Regular.fnt"));
        } else {
            font = new BitmapFont(); // Fallback por seguridad si no existe el archivo .fnt
        }
        font.setColor(Color.WHITE);

        glyphLayout = new GlyphLayout();
    }

    @Override
    public void render(float delta) {

        // Actualizar puntaje
        scoreManager.update(delta);

        Gdx.gl.glClearColor(0.5f, 0.8f, 1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        player.update(delta);
        piso.update(delta);
        batch.begin();

        for (ParallaxLayer layer : backgroundLayers) {
            layer.updateAndDraw(
                batch,
                gameSpeed,
                delta,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight()
            );
        }
        piso.draw(batch);
        player.draw(batch);

        // Dibujar el puntaje en la esquina superior derecha
        String textoPuntaje = "SCORE " + scoreManager.getScoreFormateado();
        glyphLayout.setText(font, textoPuntaje);

        float xTexto = Gdx.graphics.getWidth() - glyphLayout.width - 20f;
        float yTexto = Gdx.graphics.getHeight() - 20f;

        font.draw(batch, textoPuntaje, xTexto, yTexto);

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
        font.dispose();
        player.dispose();
        piso.dispose();
        for (ParallaxLayer layer : backgroundLayers) {
            layer.dispose();
        }
    }
}
