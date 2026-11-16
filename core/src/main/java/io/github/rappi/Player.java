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
    private Sound sonidoSalto;

    private float x;
    private float y;
    private Rectangle zonaColision; // Hitbox del jugador

    // Físicas del salto
    private float velocidadY;
    private final float gravedad = -1200f;
    private final float fuerzaSalto = 600f;
    private boolean enSuelo;

    // Control de los 3 carriles
    private int carrilDestino = 1; // 0 = Abajo, 1 = Medio, 2 = Arriba
    private float[] posicionesY = new float[3];
    private float ySueloActual;

    private final float ancho = 103f;
    private final float alto = 103f;

    public Player(Piso piso, int indicePersonaje) {
        String archivoSuelo = "RepartidorSprites/jugador_suelo_" + indicePersonaje + ".png";
        String archivoAire = "RepartidorSprites/jugador_aire_" + indicePersonaje + ".png";
        String archivoCaida = "RepartidorSprites/jugador_caida_" + indicePersonaje + ".png";

        if (Gdx.files.internal(archivoSuelo).exists()) {
            jugador = new Texture(Gdx.files.internal(archivoSuelo));
        } else {
            jugador = new Texture(Gdx.files.internal("RepartidorSprites/jugador_suelo.png"));
        }

        if (Gdx.files.internal(archivoAire).exists()) {
            jugadorSalto = new Texture(Gdx.files.internal(archivoAire));
        } else {
            jugadorSalto = new Texture(Gdx.files.internal("RepartidorSprites/jugador_aire.png"));
        }

        if (Gdx.files.internal(archivoCaida).exists()) {
            jugadorCaida = new Texture(Gdx.files.internal(archivoCaida));
        } else if (Gdx.files.internal("RepartidorSprites/jugador_caida.png").exists()) {
            jugadorCaida = new Texture(Gdx.files.internal("RepartidorSprites/jugador_caida.png"));
        } else {
            jugadorCaida = jugadorSalto;
        }

        for (int carril = 0; carril < posicionesY.length; carril++) {
            posicionesY[carril] = piso.getPosYCarril(carril);
        }

        zonaColision = new Rectangle(0f, 0f, ancho - 70f, alto - 30f);
        reset();

        if (Gdx.files.internal("Sonidos/jump.mp3").exists()) {
            sonidoSalto = Gdx.audio.newSound(Gdx.files.internal("Sonidos/jump.mp3"));
        }
    }

    private float alturaSalto;

    public void reset() {
        x = 100f;
        carrilDestino = 1;
        ySueloActual = posicionesY[carrilDestino];
        y = ySueloActual;
        alturaSalto = 0f;
        velocidadY = 0f;
        enSuelo = true;
        zonaColision.setPosition(x + 35f, y + 15f);
    }

    public void update(float segundosTranscurridos) {
        update(segundosTranscurridos, 100f);
    }

    public void update(float segundosTranscurridos, float destinoX) {
        float paso = 180f * segundosTranscurridos;
        x += MathUtils.clamp(destinoX - x, -paso, paso);

        // 1. SALTO FÍSICO (ESPACIO)
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && enSuelo) {
            velocidadY = fuerzaSalto;
            enSuelo = false;

            if (sonidoSalto != null) {
                sonidoSalto.stop();
                sonidoSalto.play(0.8f);

            }
        }

        // 2. CAMBIO DE CARRIL (ARRIBA / ABAJO) - Solo en el suelo
        if (enSuelo) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                if (carrilDestino < 2) {
                    carrilDestino++;
                }
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
                if (carrilDestino > 0) {
                    carrilDestino--;
                }
            }
        }

        float avance = 1f - (float) Math.exp(-18f * segundosTranscurridos);
        ySueloActual = MathUtils.lerp(ySueloActual, posicionesY[carrilDestino], avance);
        if (!enSuelo) {
            alturaSalto += velocidadY * segundosTranscurridos + 0.5f * gravedad * segundosTranscurridos * segundosTranscurridos;
            velocidadY += gravedad * segundosTranscurridos;
            if (alturaSalto <= 0f) {
                alturaSalto = 0f;
                velocidadY = 0f;
                enSuelo = true;
            }
        }
        y = ySueloActual + alturaSalto;

        // Actualizar posición de la hitbox reducida
        zonaColision.setPosition(x + 35f, y + 15f);
    }

    public void draw(SpriteBatch loteDibujo) {
        Texture texturaActual;
        if (enSuelo) {
            texturaActual = jugador;
        } else if (velocidadY < 0) {
            texturaActual = jugadorCaida;
        } else {
            texturaActual = jugadorSalto;
        }

        loteDibujo.draw(texturaActual, x, y, ancho, alto);
    }

    public Rectangle getBounds() {
        return zonaColision;
    }

    public int getNivelActual() {
        if (ySueloActual < (posicionesY[0] + posicionesY[1]) / 2f) return 0;
        if (ySueloActual < (posicionesY[1] + posicionesY[2]) / 2f) return 1;
        return 2;
    }

    public void dispose() {
        if (jugador != null) jugador.dispose();
        if (jugadorSalto != null) jugadorSalto.dispose();
        if (jugadorCaida != null && jugadorCaida != jugadorSalto) {
            jugadorCaida.dispose();
        }
        if (sonidoSalto != null) {
            sonidoSalto.dispose();
        }
    }
}
