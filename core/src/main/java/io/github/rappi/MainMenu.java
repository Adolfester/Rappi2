package io.github.rappi;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class MainMenu implements Screen {

    private final Main juego;
    private SpriteBatch loteDibujo;
    private BitmapFont fuente;
    private GlyphLayout medidasTexto;
    private OrthographicCamera camara;
    private Viewport vista;
    private ShapeRenderer dibujanteFormas;

    private Rectangle botonJugar;
    private Rectangle botonPersonaje;
    private final Rectangle botonOpciones = new Rectangle(540f, 190f, 200f, 50f);
    private final Rectangle botonVolver = new Rectangle(540f, 95f, 200f, 50f);
    private static final int[][] TAMANOS = {
        {960, 540}, {1280, 720}, {1600, 900}, {1920, 1080}
    };
    private final Rectangle[] botonesTamano = new Rectangle[TAMANOS.length];
    private boolean opcionesAbiertas;
    private String aviso = "";
    private Vector3 puntoPulsado;
    private Texture logotipo;
    private Music musicaMenu;
    private Sound sonidoClic;

    public MainMenu(Main juego) {
        this.juego = juego;
        logotipo = new Texture(Gdx.files.internal("Fondo/rappi.png"));
    }

    @Override
    public void show() {
        loteDibujo = new SpriteBatch();
        fuente = new BitmapFont();
        dibujanteFormas = new ShapeRenderer();
        puntoPulsado = new Vector3();

        for (int i = 0; i < botonesTamano.length; i++) {
            botonesTamano[i] = new Rectangle(510f, 440f - 68f * i, 260f, 50f);
        }
        botonJugar = new Rectangle(540f, 320f, 200f, 50f);
        botonPersonaje = new Rectangle(540f, 255f, 200, 50);

        medidasTexto = new GlyphLayout();
        camara = new OrthographicCamera();
        vista = new FitViewport(1280f, 720f, camara);

        vista.update(
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight(),
            true
        );

        // Cargar audio con resguardo si el archivo no existe aún
        if (Gdx.files.internal("Sonidos/click.mp3").exists()) {
            sonidoClic = Gdx.audio.newSound(Gdx.files.internal("Sonidos/click.mp3"));
        }

        if (Gdx.files.internal("Sonidos/menu_music.mp3").exists()) {
            musicaMenu = Gdx.audio.newMusic(Gdx.files.internal("Sonidos/menu_music.mp3"));
            musicaMenu.setLooping(true);
            musicaMenu.setVolume(0.5f);
            musicaMenu.play();
        }
    }

    @Override
    public void render(float segundosTranscurridos) {
        if (opcionesAbiertas) {
            renderOpciones();
            return;
        }
        if (Gdx.input.justTouched()) {
            puntoPulsado.set(
                Gdx.input.getX(),
                Gdx.input.getY(),
                0
            );

            vista.unproject(puntoPulsado);

            if (botonOpciones.contains(puntoPulsado.x, puntoPulsado.y)) {
                opcionesAbiertas = true;
                aviso = "";
                return;
            }

            if (botonJugar.contains(puntoPulsado.x, puntoPulsado.y)) {
                if (sonidoClic != null) sonidoClic.play();
                juego.setScreen(new GameScreen(juego));
                return;
            }

            if (botonPersonaje.contains(puntoPulsado.x, puntoPulsado.y)) {
                if (sonidoClic != null) sonidoClic.play();
                juego.setScreen(new CharacterSelectScreen(juego));
                return;
            }
        }

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        loteDibujo.setProjectionMatrix(camara.combined);

        float logotipoX = (vista.getWorldWidth() - 200) / 2f;
        float logotipoY = (vista.getWorldHeight() + 100) / 2f;

        dibujanteFormas.setProjectionMatrix(camara.combined);
        dibujanteFormas.begin(ShapeRenderer.ShapeType.Filled);

        // Botón Jugar (Verde)
        dibujanteFormas.setColor(0.2f, 0.7f, 0.3f, 1);
        dibujanteFormas.rect(botonJugar.x, botonJugar.y, botonJugar.width, botonJugar.height);

        // Botón Personaje (Naranja)
        dibujanteFormas.setColor(0.9f, 0.5f, 0.2f, 1);
        dibujanteFormas.rect(botonPersonaje.x, botonPersonaje.y, botonPersonaje.width, botonPersonaje.height);

        dibujarBoton(botonOpciones, false);
        dibujanteFormas.end();

        loteDibujo.begin();

        String controles = "Flechas arriba y abajo: cambiar de carril   |   Espacio: saltar";
        medidasTexto.setText(fuente, controles);
        fuente.draw(loteDibujo, medidasTexto, (vista.getWorldWidth() - medidasTexto.width) / 2f, botonOpciones.y - 28f);
        medidasTexto.setText(fuente, "Recoge pedidos para sumar 25 puntos");
        fuente.draw(loteDibujo, medidasTexto, (vista.getWorldWidth() - medidasTexto.width) / 2f, botonOpciones.y - 56f);
        textoCentrado("OPCIONES", botonOpciones.y + botonOpciones.height / 2f);
        loteDibujo.draw(logotipo, logotipoX, logotipoY, 200, 100);

        textoCentrado("JUGAR", botonJugar.y + botonJugar.height / 2f);
        textoCentrado("PERSONAJE", botonPersonaje.y + botonPersonaje.height / 2f);

        loteDibujo.end();
    }

    private void renderOpciones() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            opcionesAbiertas = false;
            return;
        }
        if (Gdx.input.justTouched()) {
            puntoPulsado.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            vista.unproject(puntoPulsado);
            if (botonVolver.contains(puntoPulsado.x, puntoPulsado.y)) {
                opcionesAbiertas = false;
                return;
            }
            if (juego.permiteCambiarVentana()) {
                for (int i = 0; i < botonesTamano.length; i++) {
                    if (botonesTamano[i].contains(puntoPulsado.x, puntoPulsado.y)) {
                        boolean aplicado = juego.cambiarVentana(TAMANOS[i][0], TAMANOS[i][1]);
                        aviso = aplicado ? "Preferencia guardada" : "No se pudo aplicar ese tamaño";
                        break;
                    }
                }
            }
        }

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        dibujanteFormas.setProjectionMatrix(camara.combined);
        dibujanteFormas.begin(ShapeRenderer.ShapeType.Filled);
        int ancho = Gdx.graphics.getWidth();
        int alto = Gdx.graphics.getHeight();
        for (int i = 0; i < botonesTamano.length; i++) {
            dibujarBoton(botonesTamano[i], ancho == TAMANOS[i][0] && alto == TAMANOS[i][1]);
        }
        dibujarBoton(botonVolver, false);
        dibujanteFormas.end();

        loteDibujo.setProjectionMatrix(camara.combined);
        loteDibujo.begin();
        textoCentrado("Tamaño de la ventana", 585f);
        textoCentrado("Actual: " + ancho + " x " + alto, 530f);
        for (int i = 0; i < botonesTamano.length; i++) {
            textoCentrado(TAMANOS[i][0] + " x " + TAMANOS[i][1],
                    botonesTamano[i].y + botonesTamano[i].height / 2f);
        }
        textoCentrado(juego.permiteCambiarVentana() ? aviso : "Disponible en escritorio", 185f);
        textoCentrado("VOLVER", botonVolver.y + botonVolver.height / 2f);
        loteDibujo.end();
    }

    private void dibujarBoton(Rectangle boton, boolean seleccionado) {
        if (seleccionado) dibujanteFormas.setColor(0.2f, 0.7f, 0.3f, 1f);
        else dibujanteFormas.setColor(0.22f, 0.32f, 0.49f, 1f);
        dibujanteFormas.rect(boton.x, boton.y, boton.width, boton.height);
    }

    private void textoCentrado(String texto, float centroY) {
        medidasTexto.setText(fuente, texto);
        fuente.draw(loteDibujo, medidasTexto, (vista.getWorldWidth() - medidasTexto.width) / 2f,
                centroY + medidasTexto.height / 2f);
    }

    @Override
    public void resize(int ancho, int alto) {
        vista.update(ancho, alto, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        if (musicaMenu != null) {
            musicaMenu.stop();
        }
    }

    @Override
    public void dispose() {
        loteDibujo.dispose();
        fuente.dispose();
        dibujanteFormas.dispose();
        logotipo.dispose();
        if (sonidoClic != null) sonidoClic.dispose();
        if (musicaMenu != null) musicaMenu.dispose();
    }

}
