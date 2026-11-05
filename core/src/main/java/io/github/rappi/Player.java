package io.github.rappi;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class Player {

    private Texture jugador;
    private Texture jugadorSalto;
    private Texture jugadorCaida;
    private Sound jumpSound;
    private boolean sonidoSaltoSonado = false;

    private float x;
    private float y;
    private Rectangle bounds; // Hitbox del jugador

    // Físicas del salto
    private float velocidadY;
    private final float gravedad = -1200f;
    private final float fuerzaSalto = 600f;
    private boolean enSuelo;

    // Control de los 3 carriles
    private int nivelActual = 1; // 0 = Abajo, 1 = Medio, 2 = Arriba
    private float[] posicionesY = new float[3];
    private float ySueloActual;

    private final float ancho = 103f;
    private final float alto = 103f;

    public Player(Piso piso, int characterId) {
        String sueloFile = "RepartidorSprites/jugador_suelo_" + characterId + ".png";
        String aireFile = "RepartidorSprites/jugador_aire_" + characterId + ".png";
        String caidaFile = "RepartidorSprites/jugador_caida_" + characterId + ".png";

        if (Gdx.files.internal(sueloFile).exists()) {
            jugador = new Texture(Gdx.files.internal(sueloFile));
        } else {
            jugador = new Texture(Gdx.files.internal("RepartidorSprites/jugador_suelo.png"));
        }

        if (Gdx.files.internal(aireFile).exists()) {
            jugadorSalto = new Texture(Gdx.files.internal(aireFile));
        } else {
            jugadorSalto = new Texture(Gdx.files.internal("RepartidorSprites/jugador_aire.png"));
        }

        if (Gdx.files.internal(caidaFile).exists()) {
            jugadorCaida = new Texture(Gdx.files.internal(caidaFile));
        } else if (Gdx.files.internal("RepartidorSprites/jugador_caida.png").exists()) {
            jugadorCaida = new Texture(Gdx.files.internal("RepartidorSprites/jugador_caida.png"));
        } else {
            jugadorCaida = jugadorSalto;
        }

        posicionesY[0] = piso.getPosYCarril(0);
        posicionesY[1] = piso.getPosYCarril(1);
        posicionesY[2] = piso.getPosYCarril(2);

        x = 100f;
        ySueloActual = posicionesY[nivelActual];
        y = ySueloActual;

        // Hitbox del jugador reducida para mayor margen de esquive
        bounds = new Rectangle(x + 35f, y + 15f, ancho - 70f, alto - 30f);

        velocidadY = 0f;
        enSuelo = true;

        if (Gdx.files.internal("Sonidos/jump.mp3").exists()) {
            jumpSound = Gdx.audio.newSound(Gdx.files.internal("Sonidos/jump.mp3"));
        }
    }

    public void update(float delta) {

        // 1. SALTO FÍSICO (ESPACIO)
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && enSuelo) {
            velocidadY = fuerzaSalto;
            enSuelo = false;
            sonidoSaltoSonado = false;

            if (jumpSound != null && !sonidoSaltoSonado) {
                jumpSound.stop();
                jumpSound.play(0.8f);
                sonidoSaltoSonado = true;
            }
        }

        // 2. CAMBIO DE CARRIL (ARRIBA / ABAJO) - Solo en el suelo
        if (enSuelo) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                if (nivelActual < 2) {
                    nivelActual++;
                }
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
                if (nivelActual > 0) {
                    nivelActual--;
                }
            }
        }

        ySueloActual = posicionesY[nivelActual];

        // 3. FÍSICAS Y DESPLAZAMIENTO
        if (!enSuelo) {
            velocidadY += gravedad * delta;
            y += velocidadY * delta;

            if (y <= ySueloActual) {
                y = ySueloActual;
                velocidadY = 0f;
                enSuelo = true;
            }
        } else {
            y = MathUtils.lerp(y, ySueloActual, 18f * delta);
        }

        // Actualizar posición de la hitbox reducida
        bounds.setPosition(x + 35f, y + 15f);
    }

    public void draw(SpriteBatch batch) {
        Texture spriteActual;
        if (enSuelo) {
            spriteActual = jugador;
        } else if (velocidadY < 0) {
            spriteActual = jugadorCaida;
        } else {
            spriteActual = jugadorSalto;
        }

        batch.draw(spriteActual, x, y, ancho, alto);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public int getNivelActual() {
        return nivelActual;
    }

    public void dispose() {
        if (jugador != null) jugador.dispose();
        if (jugadorSalto != null) jugadorSalto.dispose();
        if (jugadorCaida != null && jugadorCaida != jugadorSalto) {
            jugadorCaida.dispose();
        }
        if (jumpSound != null) {
            jumpSound.dispose();
        }
    }
}
