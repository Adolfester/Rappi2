package io.github.rappi;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public class Player {

    private Texture jugador;
    private Texture jugadorSalto;

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

    private final float ancho = 69f;
    private final float alto = 69f;

    public Player(Piso piso) {
        jugador = new Texture(Gdx.files.internal("jugador_suelo.png"));
        jugadorSalto = new Texture(Gdx.files.internal("jugador_aire.png"));

        // Obtener las posiciones exactas alineadas con la textura del suelo
        posicionesY[0] = piso.getPosYCarril(0); // Carril Abajo
        posicionesY[1] = piso.getPosYCarril(1); // Carril Medio
        posicionesY[2] = piso.getPosYCarril(2); // Carril Arriba

        x = 100f;
        ySueloActual = posicionesY[nivelActual];
        y = ySueloActual;

        velocidadY = 0f;
        enSuelo = true;
    }

    public void update(float delta) {

        // 1. SALTO FÍSICO (ESPACIO)
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && enSuelo) {
            velocidadY = fuerzaSalto;
            enSuelo = false;
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
        Texture spriteActual = enSuelo ? jugador : jugadorSalto;
        batch.draw(spriteActual, x, y, ancho, alto);
    }

    public void dispose() {
        jugador.dispose();
        jugadorSalto.dispose();
    }
}
