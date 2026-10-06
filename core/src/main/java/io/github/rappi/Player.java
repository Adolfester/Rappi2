package io.github.rappi;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Player {

    private Texture jugador;
    private Texture jugadorSalto;

    private float x;
    private float y;

    private float velocidadY;
    private final float gravedad = -1200;
    private final float fuerzaSalto = 600;
    private final float ancho = 60;
    private final float alto = 60;
    private boolean enSuelo;

    public Player() {

        jugador = new Texture(Gdx.files.internal("jugador_suelo.png"));
        jugadorSalto = new Texture(Gdx.files.internal("jugador_aire.png"));

        x = 100;
        y = 100;

        velocidadY = 0;
        enSuelo = true;
    }

    public void update(float delta) {

        // Salto
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && enSuelo) {
            velocidadY = fuerzaSalto;
            enSuelo = false;
        }

        // Gravedad
        velocidadY += gravedad * delta;

        // Movimiento vertical
        y += velocidadY * delta;

        // Suelo
        if (y <= 100) {
            y = 100;
            velocidadY = 0;
            enSuelo = true;
        }
    }

    public void draw(SpriteBatch batch) {

        Texture spriteActual;

        if (enSuelo) {
            spriteActual = jugador;
        } else {
            spriteActual = jugadorSalto;
        }

        batch.draw(spriteActual, x, y, ancho, alto);    }

    public void dispose() {
        jugador.dispose();
        jugadorSalto.dispose();
    }
}


