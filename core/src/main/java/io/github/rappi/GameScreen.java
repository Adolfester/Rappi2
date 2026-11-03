package io.github.rappi;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
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

    // --- AUDIO DE LA PARTIDA ---
    private Music gameMusic;
    private Sound score100Sound;
    private int ultimoHitoScore = 0;

    // --- SISTEMA DE CICLO DÍA / TARDE / NOCHE ---
    private static final int INTERVALO_CAMBIO = 700;    // Puntos por cada fase
    private static final float RANGO_TRANSICION = 100f; // Puntos de interpolación suave

    // Colores de cielo (Clear color)
    private final Color bgDia = new Color(0.5f, 0.8f, 1.0f, 1f);
    private final Color bgTarde = new Color(0.85f, 0.45f, 0.25f, 1f);
    private final Color bgNoche = new Color(0.05f, 0.05f, 0.2f, 1f);

    // Colores de tinte para las capas del Parallax
    private final Color tintDia = new Color(1.0f, 1.0f, 1.0f, 1f);       // Luz natural
    private final Color tintTarde = new Color(1.0f, 0.7f, 0.5f, 1f);     // Atardecer cálido
    private final Color tintNoche = new Color(0.35f, 0.35f, 0.65f, 1f);   // Noche azulada

    // Colores que se actualizarán dinámicamente en tiempo real
    private final Color bgActual = new Color();
    private final Color tintActual = new Color();

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

        player = new Player(piso, game.getSelectedCharacter());
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
        // Cargar audio de la partida
        if (Gdx.files.internal("score_100.wav").exists()) {
            score100Sound = Gdx.audio.newSound(Gdx.files.internal("score_100.wav"));
        }

        if (Gdx.files.internal("game_music.mp3").exists()) {
            gameMusic = Gdx.audio.newMusic(Gdx.files.internal("game_music.mp3"));
            gameMusic.setLooping(true);
            gameMusic.setVolume(0.4f);
            gameMusic.play();
        }
    }

    @Override
    public void render(float delta) {
        // Actualizar puntaje y velocidad
        scoreManager.update(delta);
        float velocidadActual = scoreManager.getVelocidadActual();

        // Detectar hito de cada 100 puntos
        int scoreActual = scoreManager.getScore();
        if (scoreActual >= ultimoHitoScore + 100) {
            ultimoHitoScore = (scoreActual / 100) * 100;
            if (score100Sound != null) {
                score100Sound.play(0.7f);
            }
        }
        piso.setVelocidad(velocidadActual);

        player.update(delta);
        piso.update(delta);

        // Actualizar la transición suave de clima y color
        actualizarAmbienteClimatico();

        // Actualizar matriz de proyección de la cámara en el batch
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Renderizado del fondo usando el color de cielo dinámico
        Gdx.gl.glClearColor(bgActual.r, bgActual.g, bgActual.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        // Renderizar fondos aplicando el tinte de luz actual
        for (ParallaxLayer layer : backgroundLayers) {
            layer.updateAndDraw(
                batch,
                velocidadActual,
                delta,
                viewport.getWorldWidth(),
                viewport.getWorldHeight(),
                tintActual
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

    /**
     * Calcula la transición entre Día -> Tarde -> Noche según el puntaje acumulado.
     */
    private void actualizarAmbienteClimatico() {
        int score = scoreManager.getScore();

        // Determina la fase actual (0: Día, 1: Tarde, 2: Noche)
        int faseActual = (score / INTERVALO_CAMBIO) % 3;
        int siguienteFase = (faseActual + 1) % 3;

        // Puntos recorridos dentro de la fase actual
        float puntosEnFase = score % INTERVALO_CAMBIO;

        Color colorBgOrigen = getBgColor(faseActual);
        Color colorBgDestino = getBgColor(siguienteFase);

        Color colorTintOrigen = getTintColor(faseActual);
        Color colorTintDestino = getTintColor(siguienteFase);

        // Transición suave en los últimos 100 puntos de la fase
        if (puntosEnFase >= (INTERVALO_CAMBIO - RANGO_TRANSICION)) {
            float progreso = (puntosEnFase - (INTERVALO_CAMBIO - RANGO_TRANSICION)) / RANGO_TRANSICION;
            progreso = MathUtils.clamp(progreso, 0f, 1f);

            bgActual.set(colorBgOrigen).lerp(colorBgDestino, progreso);
            tintActual.set(colorTintOrigen).lerp(colorTintDestino, progreso);
        } else {
            bgActual.set(colorBgOrigen);
            tintActual.set(colorTintOrigen);
        }
    }

    private Color getBgColor(int fase) {
        switch (fase) {
            case 1: return bgTarde;
            case 2: return bgNoche;
            default: return bgDia;
        }
    }

    private Color getTintColor(int fase) {
        switch (fase) {
            case 1: return tintTarde;
            case 2: return tintNoche;
            default: return tintDia;
        }
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
    public void hide() {
        if (gameMusic != null) {
            gameMusic.stop();
        }
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
        if (score100Sound != null) score100Sound.dispose();
        if (gameMusic != null) gameMusic.dispose();
    }
}
