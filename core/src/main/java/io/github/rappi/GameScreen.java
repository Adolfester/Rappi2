package io.github.rappi;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameScreen implements Screen {

    private final Main game;

    private SpriteBatch batch;
    private Player player;
    private Array<ParallaxLayer> backgroundLayers;
    private Piso piso;

    // --- GESTIÓN DE OBSTÁCULOS ---
    private Array<Obstaculos> obstaculos;
    private float tiempoSpawn = 0f;
    private float siguienteSpawnIntervalo = 2.0f;

    // Cámara y Viewport para escalado automático
    private OrthographicCamera camera;
    private Viewport viewport;
    private static final float WORLD_WIDTH = 1280f;
    private static final float WORLD_HEIGHT = 720f;

    // Gestor de puntaje y fuente bitmap
    private ScoreManager scoreManager;
    private BitmapFont font;
    private GlyphLayout glyphLayout;

    // --- AUDIO DE LA PARTIDA ---
    private Music gameMusic;
    private Sound score100Sound;
    private int ultimoHitoScore = 0;

    // --- SISTEMA DE ESTADOS DEL JUEGO ---
    private enum EstadoJuego {
        JUGANDO,
        GAME_OVER
    }
    private EstadoJuego estadoActual = EstadoJuego.JUGANDO;

    private ShapeRenderer shapeRenderer;
    private Rectangle botonReiniciar;
    private Vector3 touchPoint;

    // --- CLASE AUXILIAR INTERNA PARA EL ORDENAMIENTO (Y-SORTING) ---
    private static class ElementoVisual {
        Object objeto;
        float y;
        boolean esJugador;

        public ElementoVisual(Object objeto, float y, boolean esJugador) {
            this.objeto = objeto;
            this.y = y;
            this.esJugador = esJugador;
        }
    }

    // --- SISTEMA DE CICLO DÍA / TARDE / NOCHE ---
    private static final int INTERVALO_CAMBIO = 700;
    private static final float RANGO_TRANSICION = 100f;

    private final Color bgDia = new Color(0.5f, 0.8f, 1.0f, 1f);
    private final Color bgTarde = new Color(0.85f, 0.45f, 0.25f, 1f);
    private final Color bgNoche = new Color(0.05f, 0.05f, 0.2f, 1f);

    private final Color tintDia = new Color(1.0f, 1.0f, 1.0f, 1f);
    private final Color tintTarde = new Color(1.0f, 0.7f, 0.5f, 1f);
    private final Color tintNoche = new Color(0.35f, 0.35f, 0.65f, 1f);

    private final Color bgActual = new Color();
    private final Color tintActual = new Color();

    public GameScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        touchPoint = new Vector3();
        botonReiniciar = new Rectangle(0, 0, 300, 70);

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply();

        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);
        camera.update();

        scoreManager = new ScoreManager(300f);
        piso = new Piso(40f, 4.0f);
        player = new Player(piso, game.getSelectedCharacter());

        obstaculos = new Array<>();
        tiempoSpawn = 0f;
        siguienteSpawnIntervalo = 2.2f;

        backgroundLayers = new Array<>();
        backgroundLayers.add(new ParallaxLayer("Fondo/1.png", 0.05f));
        backgroundLayers.add(new ParallaxLayer("Fondo/2.png", 0.15f));
        backgroundLayers.add(new ParallaxLayer("Fondo/3.png", 0.35f));
        backgroundLayers.add(new ParallaxLayer("Fondo/4.png", 0.70f));
        backgroundLayers.add(new ParallaxLayer("Fondo/5.png", 0.80f));

        if (Gdx.files.internal("8bitOperatorPlus-Regular.fnt").exists()) {
            font = new BitmapFont(Gdx.files.internal("8bitOperatorPlus-Regular.fnt"));
        } else {
            font = new BitmapFont();
        }
        font.setColor(Color.WHITE);

        glyphLayout = new GlyphLayout();

        if (Gdx.files.internal("Sonidos/score_100.mp3").exists()) {
            score100Sound = Gdx.audio.newSound(Gdx.files.internal("Sonidos/score_100.mp3"));
        }

        iniciarMusicaJuego();
    }

    private void iniciarMusicaJuego() {
        if (Gdx.files.internal("Sonidos/game_music.mp3").exists()) {
            gameMusic = Gdx.audio.newMusic(Gdx.files.internal("Sonidos/game_music.mp3"));
            gameMusic.setLooping(true);
            gameMusic.setVolume(0.4f);
            gameMusic.play();
        }
    }

    @Override
    public void render(float delta) {
        // --- 1. LÓGICA DE JUEGO ---
        if (estadoActual == EstadoJuego.JUGANDO) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.G)) {
                activarGameOver();
            }

            scoreManager.update(delta);
            float velocidadActual = scoreManager.getVelocidadActual();

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

            // --- GENERACIÓN PROCEDURAL DE OBSTÁCULOS ---
            tiempoSpawn += delta;
            float intervaloDinamico = Math.max(0.8f, siguienteSpawnIntervalo - (scoreActual / 600f));

            if (tiempoSpawn >= intervaloDinamico) {
                tiempoSpawn = 0f;
                int carrilAleatorio = MathUtils.random(0, 2);
                obstaculos.add(new Obstaculos(piso, carrilAleatorio, viewport.getWorldWidth()));
                siguienteSpawnIntervalo = MathUtils.random(1.5f, 2.5f);
            }

            // Actualizar obstáculos y comprobar colisión por carril
            for (int i = obstaculos.size - 1; i >= 0; i--) {
                Obstaculos obs = obstaculos.get(i);
                obs.update(delta, velocidadActual);

                if (player.getNivelActual() == obs.getCarril() && player.getBounds().overlaps(obs.getBounds())) {
                    activarGameOver();
                }

                if (obs.estaFueraDePantalla()) {
                    obs.dispose();
                    obstaculos.removeIndex(i);
                }
            }

            actualizarAmbienteClimatico();

        } else if (estadoActual == EstadoJuego.GAME_OVER) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                reiniciarJuego();
            }

            if (Gdx.input.justTouched()) {
                touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
                viewport.unproject(touchPoint);

                if (botonReiniciar.contains(touchPoint.x, touchPoint.y)) {
                    reiniciarJuego();
                }
            }
        }

        // --- 2. RENDERIZADO ---
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        Gdx.gl.glClearColor(bgActual.r, bgActual.g, bgActual.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        float velocidadParallax = (estadoActual == EstadoJuego.JUGANDO) ? scoreManager.getVelocidadActual() : 0f;

        for (ParallaxLayer layer : backgroundLayers) {
            layer.updateAndDraw(
                batch,
                velocidadParallax,
                delta,
                viewport.getWorldWidth(),
                viewport.getWorldHeight(),
                tintActual
            );
        }

        piso.draw(batch, viewport.getWorldWidth());

        // --- RENDERIZADO CON ORDENAMIENTO POR PROFUNDIDAD (Y-SORTING INTERNO) ---
        Array<ElementoVisual> elementosVisuales = new Array<>();
        elementosVisuales.add(new ElementoVisual(player, player.getBounds().y, true));
        for (Obstaculos obs : obstaculos) {
            elementosVisuales.add(new ElementoVisual(obs, obs.getBounds().y, false));
        }

        // Ordenar de mayor Y a menor Y (los de arriba primero, los de abajo al final)
        elementosVisuales.sort((e1, e2) -> Float.compare(e2.y, e1.y));

        // Dibujar en el orden correcto
        for (ElementoVisual el : elementosVisuales) {
            if (el.esJugador) {
                player.draw(batch);
            } else {
                ((Obstaculos) el.objeto).draw(batch);
            }
        }

        // Dibujar el puntaje
        String textoPuntaje = "SCORE " + scoreManager.getScoreFormateado();
        glyphLayout.setText(font, textoPuntaje);

        float xTexto = viewport.getWorldWidth() - glyphLayout.width - 30f;
        float yTexto = viewport.getWorldHeight() - 30f;

        font.draw(batch, textoPuntaje, xTexto, yTexto);

        batch.end();

        // --- 3. OVERLAY DE GAME OVER ---
        if (estadoActual == EstadoJuego.GAME_OVER) {
            shapeRenderer.setProjectionMatrix(camera.combined);

            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.6f, 0.0f, 0.0f, 0.5f);
            shapeRenderer.rect(0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

            float botonX = (viewport.getWorldWidth() - botonReiniciar.width) / 2f;
            float botonY = viewport.getWorldHeight() / 2f - 120f;
            botonReiniciar.setPosition(botonX, botonY);

            shapeRenderer.setColor(0.2f, 0.7f, 0.3f, 1f);
            shapeRenderer.rect(botonReiniciar.x, botonReiniciar.y, botonReiniciar.width, botonReiniciar.height);
            shapeRenderer.end();

            Gdx.gl.glDisable(GL20.GL_BLEND);

            batch.begin();
            font.getData().setScale(2.5f);

            String textoGameOver = "GAME OVER";
            glyphLayout.setText(font, textoGameOver);
            float xGameOver = (viewport.getWorldWidth() - glyphLayout.width) / 2f;
            float yGameOver = viewport.getWorldHeight() / 2f + 80f;
            font.draw(batch, textoGameOver, xGameOver, yGameOver);

            font.getData().setScale(1.2f);
            String textoBoton = "REINTENTAR";
            glyphLayout.setText(font, textoBoton);
            float xTextoBoton = botonReiniciar.x + (botonReiniciar.width - glyphLayout.width) / 2f;
            float yTextoBoton = botonReiniciar.y + (botonReiniciar.height + glyphLayout.height) / 2f;
            font.draw(batch, textoBoton, xTextoBoton, yTextoBoton);

            font.getData().setScale(1.0f);
            batch.end();
        }
    }

    private void activarGameOver() {
        if (estadoActual == EstadoJuego.JUGANDO) {
            estadoActual = EstadoJuego.GAME_OVER;
            if (gameMusic != null) {
                gameMusic.stop();
            }
        }
    }

    private void reiniciarJuego() {
        for (Obstaculos obs : obstaculos) {
            obs.dispose();
        }
        obstaculos.clear();

        scoreManager = new ScoreManager(300f);
        piso = new Piso(40f, 4.0f);
        player = new Player(piso, game.getSelectedCharacter());
        ultimoHitoScore = 0;
        tiempoSpawn = 0f;
        estadoActual = EstadoJuego.JUGANDO;

        if (gameMusic != null) {
            gameMusic.play();
        }
    }

    private void actualizarAmbienteClimatico() {
        int score = scoreManager.getScore();

        int faseActual = (score / INTERVALO_CAMBIO) % 3;
        int siguienteFase = (faseActual + 1) % 3;

        float puntosEnFase = score % INTERVALO_CAMBIO;

        Color colorBgOrigen = getBgColor(faseActual);
        Color colorBgDestino = getBgColor(siguienteFase);

        Color colorTintOrigen = getTintColor(faseActual);
        Color colorTintDestino = getTintColor(siguienteFase);

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
        shapeRenderer.dispose();

        for (Obstaculos obs : obstaculos) {
            obs.dispose();
        }

        for (ParallaxLayer layer : backgroundLayers) {
            layer.dispose();
        }
        if (score100Sound != null) score100Sound.dispose();
        if (gameMusic != null) gameMusic.dispose();
    }
}
