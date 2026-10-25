package io.github.rappi;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameScreen implements Screen {

    private final Main game;

    private SpriteBatch batch;
    private Player player;
    private Array<ParallaxLayer> backgroundLayers;
    private Piso piso;

    // Cámara y Viewport para escalado automático
    private OrthographicCamera camera;
    private Viewport viewport;
    private static final float WORLD_WIDTH = 1280f;  // Ancho lógico del juego
    private static final float WORLD_HEIGHT = 720f; // Alto lógico del juego

    // Gestor de puntaje y fuente bitmap
    private ScoreManager scoreManager;
    private BitmapFont font;
    private GlyphLayout glyphLayout;

    public GameScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();

        // Inicializamos cámara y viewport con dimensiones fijas
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply();

        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);
        camera.update();

        // 1. Inicializamos ScoreManager con la velocidad base (300f)
        scoreManager = new ScoreManager(300f);

        // 2. Creamos el piso
        piso = new Piso(40f, 4.0f);

        player = new Player(piso);
        backgroundLayers = new Array<>();

        backgroundLayers.add(new ParallaxLayer("1.png", 0.05f));
        backgroundLayers.add(new ParallaxLayer("2.png", 0.15f));
        backgroundLayers.add(new ParallaxLayer("3.png", 0.35f));
        backgroundLayers.add(new ParallaxLayer("4.png", 0.70f));
        backgroundLayers.add(new ParallaxLayer("5.png", 0.80f));

        // Cargar fuente pixelada
        if (Gdx.files.internal("8bitOperatorPlus-Regular.fnt").exists()) {
            font = new BitmapFont(Gdx.files.internal("8bitOperatorPlus-Regular.fnt"));
        } else {
            font = new BitmapFont();
        }
        font.setColor(Color.WHITE);

        glyphLayout = new GlyphLayout();
    }

    @Override
    public void render(float delta) {
        // Actualizar puntaje y velocidad
        scoreManager.update(delta);
        float velocidadActual = scoreManager.getVelocidadActual();

        piso.setVelocidad(velocidadActual);

        player.update(delta);
        piso.update(delta);

        // Actualizar matriz de proyección de la cámara en el batch
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Renderizado
        Gdx.gl.glClearColor(0.5f, 0.8f, 1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        // Renderizar fondos usando el ancho y alto del MUNDO VIRTUAL
        for (ParallaxLayer layer : backgroundLayers) {
            layer.updateAndDraw(
                batch,
                velocidadActual,
                delta,
                viewport.getWorldWidth(),
                viewport.getWorldHeight()
            );
        }

        // Dibujar el piso pasando el ancho del mundo virtual
        piso.draw(batch, viewport.getWorldWidth());
        player.draw(batch);

        // Dibujar el puntaje usando las medidas del mundo virtual (esquina superior derecha fija)
        String textoPuntaje = "SCORE " + scoreManager.getScoreFormateado();
        glyphLayout.setText(font, textoPuntaje);

        float xTexto = viewport.getWorldWidth() - glyphLayout.width - 30f;
        float yTexto = viewport.getWorldHeight() - 30f;

        font.draw(batch, textoPuntaje, xTexto, yTexto);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        // Vital: actualiza el viewport cuando el usuario redimensiona la ventana
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

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
