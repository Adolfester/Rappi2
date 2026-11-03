package io.github.rappi;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public class Player {

    private Texture jugador;
    private Texture jugadorSalto;
    private Texture jugadorCaida;
    private Sound jumpSound;



    private float x;
    private float y;

    // Físicas del salto
    private float velocidadY;
    private final float gravedad = -1200f;
    private final float fuerzaSalto = 600f;
    private boolean enSuelo;

    // Control de los 3 carriles
    private int nivelActual = 1; // 0 = Abajo, 1 = Medio, 2 = Arriba
    private float[] posicionesY = new float[3]; // Guarda la altura de los 3 carriles
    private float ySueloActual;

    private final float ancho = 103f;
    private final float alto = 103f;

    public Player(Piso piso, int characterId) {
        String sueloFile = "jugador_suelo_" + characterId + ".png";
        String aireFile = "jugador_aire_" + characterId + ".png";
        String caidaFile = "jugador_caida_" + characterId + ".png";

        if (Gdx.files.internal(sueloFile).exists()) {
            jugador = new Texture(Gdx.files.internal(sueloFile));
        } else {
            jugador = new Texture(Gdx.files.internal("jugador_suelo.png"));
        }

        if (Gdx.files.internal(aireFile).exists()) {
            jugadorSalto = new Texture(Gdx.files.internal(aireFile));
        } else {
            jugadorSalto = new Texture(Gdx.files.internal("jugador_aire.png"));
        }

        // Cargar sprite de caída (o usar el de salto como respaldo)
        if (Gdx.files.internal(caidaFile).exists()) {
            jugadorCaida = new Texture(Gdx.files.internal(caidaFile));
        } else if (Gdx.files.internal("jugador_caida.png").exists()) {
            jugadorCaida = new Texture(Gdx.files.internal("jugador_caida.png"));
        } else {
            jugadorCaida = jugadorSalto;
        }

        // Obtener las posiciones exactas alineadas con la textura del suelo
        posicionesY[0] = piso.getPosYCarril(0); // Carril Abajo
        posicionesY[1] = piso.getPosYCarril(1); // Carril Medio
        posicionesY[2] = piso.getPosYCarril(2); // Carril Arriba

        x = 100f;
        ySueloActual = posicionesY[nivelActual];
        y = ySueloActual;

        velocidadY = 0f;
        enSuelo = true;

        if (Gdx.files.internal("jump.wav").exists()) {
            jumpSound = Gdx.audio.newSound(Gdx.files.internal("jump.wav"));
        }
    }

    public void update(float delta) {

        // 1. SALTO FÍSICO (ESPACIO)
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && enSuelo) {
            velocidadY = fuerzaSalto;
            enSuelo = false;

            if (jumpSound != null) {
                jumpSound.play(0.8f);
            }
        }

        // 2. CAMBIO DE CARRIL (ARRIBA / ABAJO) - Solo si está en el suelo
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

        // Actualizar la altura base según el carril seleccionado
        ySueloActual = posicionesY[nivelActual];

        // 3. FÍSICAS Y DESPLAZAMIENTO
        if (!enSuelo) {
            velocidadY += gravedad * delta;
            y += velocidadY * delta;

            // Al caer, aterriza en la altura del carril actual
            if (y <= ySueloActual) {
                y = ySueloActual;
                velocidadY = 0f;
                enSuelo = true;
            }
        } else {
            // Desplazamiento suave entre carriles cuando está en el suelo
            y = MathUtils.lerp(y, ySueloActual, 18f * delta);
        }
    }

    public void draw(SpriteBatch batch) {
        Texture spriteActual;
        if (enSuelo) {
            spriteActual = jugador;
        } else if (velocidadY < 0) {
            // Pasó la altura máxima del salto y va descendiendo
            spriteActual = jugadorCaida;
        } else {
            // Está en fase ascendente del salto
            spriteActual = jugadorSalto;
        }

        batch.draw(spriteActual, x, y, ancho, alto);
    }

    public void dispose() {
        jugador.dispose();
        jugadorSalto.dispose();
        if (jugadorCaida != null && jugadorCaida != jugadorSalto) {
            jugadorCaida.dispose();
        }
        if (jumpSound != null) {
            jumpSound.dispose();
        }
    }
}
