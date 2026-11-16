package io.github.rappi;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class CharacterSelectScreen implements Screen {

    private final Main juego;
    private SpriteBatch loteDibujo;
    private ShapeRenderer dibujanteFormas;
    private BitmapFont fuente;
    private GlyphLayout medidasTexto;

    private OrthographicCamera camara;
    private Viewport vista;
    private Vector3 puntoPulsado;

    private static final int CANTIDAD_PERSONAJES = 4;
    private final String[] etiquetas = new String[CANTIDAD_PERSONAJES];
    private Texture[] texturasPersonajes;
    private Rectangle[] recuadrosPersonajes;
    private Rectangle botonVolver;

    public CharacterSelectScreen(Main juego) {
        this.juego = juego;
    }

    @Override
    public void show() {
        loteDibujo = new SpriteBatch();
        dibujanteFormas = new ShapeRenderer();
        if (Gdx.files.internal("8bitOperatorPlus-Regular.fnt").exists()) {
            fuente = new BitmapFont(Gdx.files.internal("8bitOperatorPlus-Regular.fnt"));
        } else {
            fuente = new BitmapFont();
        }
        medidasTexto = new GlyphLayout();
        puntoPulsado = new Vector3();

        camara = new OrthographicCamera();
        vista = new FitViewport(1280, 720, camara);

        texturasPersonajes = new Texture[CANTIDAD_PERSONAJES];
        recuadrosPersonajes = new Rectangle[CANTIDAD_PERSONAJES];

        for (int i = 0; i < CANTIDAD_PERSONAJES; i++) {
            etiquetas[i] = "P" + (i + 1);
            String nombreArchivo = "RepartidorSprites/jugador_suelo_" + i + ".png";
            if (Gdx.files.internal(nombreArchivo).exists()) {
                texturasPersonajes[i] = new Texture(Gdx.files.internal(nombreArchivo));
            } else {
                texturasPersonajes[i] = new Texture(Gdx.files.internal("RepartidorSprites/jugador_suelo.png"));
            }
        }

        float anchoRecuadro = 140f;
        float altoRecuadro = 140f;
        float separacion = 30f;
        float anchoTotal = (CANTIDAD_PERSONAJES * anchoRecuadro) + ((CANTIDAD_PERSONAJES - 1) * separacion);
        float inicioX = (1280f - anchoTotal) / 2f;
        float inicioY = (720f - altoRecuadro) / 2f;

        for (int i = 0; i < CANTIDAD_PERSONAJES; i++) {
            recuadrosPersonajes[i] = new Rectangle(inicioX + i * (anchoRecuadro + separacion), inicioY, anchoRecuadro, altoRecuadro);
        }

        botonVolver = new Rectangle((1280f - 200f) / 2f, 100f, 200f, 50f);
    }

    @Override
    public void render(float segundosTranscurridos) {
        if (Gdx.input.justTouched()) {
            puntoPulsado.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            vista.unproject(puntoPulsado);

            for (int i = 0; i < CANTIDAD_PERSONAJES; i++) {
                if (recuadrosPersonajes[i].contains(puntoPulsado.x, puntoPulsado.y)) {
                    juego.setSelectedCharacter(i);
                    break;
                }
            }

            if (botonVolver.contains(puntoPulsado.x, puntoPulsado.y)) {
                juego.setScreen(new MainMenu(juego));
                return;
            }
        }

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        dibujanteFormas.setProjectionMatrix(camara.combined);

        dibujanteFormas.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < CANTIDAD_PERSONAJES; i++) {
            if (juego.getSelectedCharacter() == i) {
                dibujanteFormas.setColor(0.9f, 0.8f, 0.2f, 1);
            } else {
                dibujanteFormas.setColor(0.2f, 0.25f, 0.35f, 1);
            }
            dibujanteFormas.rect(recuadrosPersonajes[i].x, recuadrosPersonajes[i].y, recuadrosPersonajes[i].width, recuadrosPersonajes[i].height);
        }

        dibujanteFormas.setColor(0.8f, 0.3f, 0.3f, 1);
        dibujanteFormas.rect(botonVolver.x, botonVolver.y, botonVolver.width, botonVolver.height);
        dibujanteFormas.end();

        dibujanteFormas.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(3);
        dibujanteFormas.setColor(Color.WHITE);
        for (int i = 0; i < CANTIDAD_PERSONAJES; i++) {
            dibujanteFormas.rect(recuadrosPersonajes[i].x + 5, recuadrosPersonajes[i].y + 5, recuadrosPersonajes[i].width - 10, recuadrosPersonajes[i].height - 10);
        }
        dibujanteFormas.end();

        loteDibujo.setProjectionMatrix(camara.combined);
        loteDibujo.begin();

        medidasTexto.setText(fuente, "SELECCIONA TU PERSONAJE");
        fuente.draw(loteDibujo, medidasTexto, (1280f - medidasTexto.width) / 2f, 600f);

        for (int i = 0; i < CANTIDAD_PERSONAJES; i++) {
            float anchoImagen = 80f;
            float altoImagen = 80f;
            float imagenX = recuadrosPersonajes[i].x + (recuadrosPersonajes[i].width - anchoImagen) / 2f;
            float imagenY = recuadrosPersonajes[i].y + (recuadrosPersonajes[i].height - altoImagen) / 2f + 10f;

            loteDibujo.draw(texturasPersonajes[i], imagenX, imagenY, anchoImagen, altoImagen);

            medidasTexto.setText(fuente, etiquetas[i]);
            fuente.draw(loteDibujo, medidasTexto, recuadrosPersonajes[i].x + (recuadrosPersonajes[i].width - medidasTexto.width) / 2f, recuadrosPersonajes[i].y + 25f);
        }

        medidasTexto.setText(fuente, "VOLVER");
        fuente.draw(loteDibujo, medidasTexto, botonVolver.x + (botonVolver.width - medidasTexto.width) / 2f, botonVolver.y + (botonVolver.height + medidasTexto.height) / 2f);

        loteDibujo.end();
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
    public void hide() {}

    @Override
    public void dispose() {
        loteDibujo.dispose();
        dibujanteFormas.dispose();
        fuente.dispose();
        for (Texture textura : texturasPersonajes) {
            if (textura != null) textura.dispose();
        }
    }
}
