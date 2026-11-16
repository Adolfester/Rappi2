package io.github.rappi;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

final class Patrullero {
    enum Fase { AUSENTE, ALERTA, PERSECUCION, EMBESTIDA, IMPACTO }

    private static final float ANCHO = 210f;
    private static final float ALTO = ANCHO * 640f / 1643f;
    private static final float DURACION_ALERTA = 5f;
    private static final float DURACION_PERSECUCION = 30f;
    private static final float INTERVALO_DISPARO = 2.4f;
    private static final float ANTICIPACION = 0.8f;
    private static final float VELOCIDAD_BALA = 320f;
    private final Piso piso;
    private final float anchoMundo;
    private final Texture coche = cargarImagen("Policia/policia_inicio.png");
    private final Texture policia = cargarImagen("Policia/policia_boss.png");
    private final Music sirena = cargarMusica("Sonidos/sirena.mp3", 0.55f);
    private final Music musica = cargarMusica("Sonidos/persecucion.mp3", 0.45f);
    private final Sound sonidoDisparo = Gdx.audio.newSound(Gdx.files.internal("Sonidos/disparo.wav"));
    private final Texture pixel;
    private final Array<Bala> balas = new Array<>();
    private Fase fase = Fase.AUSENTE;
    private int siguienteHito = 1000;
    private int carrilApuntado;
    private float posicionX, posicionY, tiempo, hastaDisparo;
    private Obstaculos objetivo;

    private static final class Bala {
        final Rectangle zona;
        final int carril;
        Bala(float x, float y, int carril) {
            zona = new Rectangle(x, y, 18f, 6f);
            this.carril = carril;
        }
    }

    Patrullero(Piso piso, float anchoMundo) {
        this.piso = piso;
        this.anchoMundo = anchoMundo;
        Pixmap imagen = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        imagen.setColor(1f, 1f, 1f, 1f);
        imagen.fill();
        pixel = new Texture(imagen);
        imagen.dispose();
    }

    private static Texture cargarImagen(String ruta) {
        return new Texture(Gdx.files.internal(ruta));
    }

    private static Music cargarMusica(String ruta, float volumen) {
        Music pista = Gdx.audio.newMusic(Gdx.files.internal(ruta));
        pista.setVolume(volumen);
        pista.setLooping(true);
        return pista;
    }

    boolean estaEmbistiendo(Obstaculos obstaculo) { return objetivo == obstaculo; }

    Fase obtenerFase() { return fase; }

    // El repartidor deja espacio para ver el patrullero detrás de él.
    float destinoJugador() { return fase == Fase.AUSENTE ? 100f : 420f; }

    int segundosRestantes() {
        return (int) Math.ceil(Math.max(0f, DURACION_PERSECUCION - tiempo));
    }

    private int carrilActual() {
        float separacion = piso.getPosYCarril(1) - piso.getPosYCarril(0);
        return MathUtils.clamp(Math.round((posicionY - piso.getPosYCarril(0)) / separacion), 0, 2);
    }

    boolean actualizar(float segundos, float avancePista, int puntaje, Player jugador, Array<Obstaculos> obstaculos) {
        if (fase == Fase.AUSENTE) {
            if (puntaje >= siguienteHito) {
                siguienteHito += 1000;
                fase = Fase.ALERTA;
                tiempo = 0f;
                posicionX = -ANCHO;
                posicionY = piso.getPosYCarril(jugador.getNivelActual());
                sirena.play();
            }
            return false;
        }
        tiempo += segundos;
        switch (fase) {
            case ALERTA:
                posicionX = Math.min(12f, posicionX + 145f * segundos);
                if (tiempo >= DURACION_ALERTA) {
                    sirena.stop();
                    musica.play();
                    fase = Fase.PERSECUCION;
                    tiempo -= DURACION_ALERTA;
                    hastaDisparo = INTERVALO_DISPARO;
                    carrilApuntado = jugador.getNivelActual();
                }
                break;
            case PERSECUCION:
                if (tiempo >= DURACION_PERSECUCION) {
                    balas.clear();
                    fase = Fase.EMBESTIDA;
                    actualizarEmbestida(segundos, avancePista, obstaculos);
                    break;
                }
                // Sigue al jugador hasta fijar el carril del próximo disparo.
                if (hastaDisparo > ANTICIPACION) carrilApuntado = jugador.getNivelActual();
                float paso = (piso.getPosYCarril(1) - piso.getPosYCarril(0)) * 3f * segundos;
                float diferencia = piso.getPosYCarril(carrilApuntado) - posicionY;
                posicionY += MathUtils.clamp(diferencia, -paso, paso);
                hastaDisparo -= segundos;
                if (hastaDisparo <= 0f) {
                    balas.add(new Bala(posicionX + ANCHO * 0.70f, posicionY + ALTO * 0.64f, carrilApuntado));
                    sonidoDisparo.play(0.75f);
                    hastaDisparo += INTERVALO_DISPARO;
                }
                return actualizarBalas(segundos, jugador);
            case EMBESTIDA:
                actualizarEmbestida(segundos, avancePista, obstaculos);
                break;
            case IMPACTO:
                if (tiempo >= 0.9f) fase = Fase.AUSENTE;
                break;
            default:
                break;
        }
        return false;
    }

    private boolean actualizarBalas(float segundos, Player jugador) {
        for (int i = balas.size - 1; i >= 0; i--) {
            Bala bala = balas.get(i);
            bala.zona.x += VELOCIDAD_BALA * segundos;
            if (bala.carril == jugador.getNivelActual() && bala.zona.overlaps(jugador.getBounds())) return true;
            if (bala.zona.x > anchoMundo) balas.removeIndex(i);
        }
        return false;
    }

    private Obstaculos buscarObjetivo(Array<Obstaculos> obstaculos) {
        Obstaculos mejor = null;
        int menorDiferencia = Integer.MAX_VALUE;
        float menorDistancia = Float.MAX_VALUE;
        for (Obstaculos candidato : obstaculos) {
            Rectangle zona = candidato.getBounds();
            if (zona.x + zona.width < posicionX || zona.x > anchoMundo) continue;
            int diferencia = Math.abs(candidato.getCarril() - carrilActual());
            float distancia = Math.abs(zona.x - (posicionX + ANCHO));
            if (diferencia < menorDiferencia || (diferencia == menorDiferencia && distancia < menorDistancia)) {
                mejor = candidato;
                menorDiferencia = diferencia;
                menorDistancia = distancia;
            }
        }
        return mejor;
    }

    private void actualizarEmbestida(float segundos, float avancePista, Array<Obstaculos> obstaculos) {
        if (objetivo == null) objetivo = buscarObjetivo(obstaculos);
        if (objetivo == null) return; // Espera al siguiente obstáculo que entre en la pista.
        float distancia = Math.max(0f, objetivo.getBounds().x - (posicionX + ANCHO - 8f));
        float avance = 650f * segundos;
        // Considerar también el avance del obstáculo hacia el patrullero.
        float proporcion = Math.min(1f, (avance + avancePista) / Math.max(1f, distancia));
        posicionY += (piso.getPosYCarril(objetivo.getCarril()) - posicionY) * proporcion;
        posicionX += Math.min(distancia, avance);
        if (distancia <= avance) {
            posicionY = piso.getPosYCarril(objetivo.getCarril());
            obstaculos.removeValue(objetivo, true);
            objetivo = null;
            fase = Fase.IMPACTO;
            tiempo = 0f;
            musica.stop();
        }
    }

    void dibujarCarril(SpriteBatch lote, int carril) {
        if (fase == Fase.AUSENTE) return;
        float colorAnterior = lote.getPackedColor();
        if (carrilActual() == carril) {
            boolean impacto = fase == Fase.IMPACTO;
            if (impacto) lote.setColor(1f, 1f - tiempo * 0.6f, 0.45f, 1f - tiempo);
            lote.draw(fase == Fase.ALERTA ? coche : policia, posicionX, posicionY, ANCHO, ALTO);
            if (impacto) {
                for (int i = 0; i < 7; i++) {
                    float angulo = i * MathUtils.PI / 6f;
                    float radio = 12f + tiempo * 65f;
                    lote.draw(pixel, posicionX + ANCHO - 12f + MathUtils.cos(angulo) * radio,
                            posicionY + 20f + MathUtils.sin(angulo) * radio, 7f, 7f);
                }
            } else if (fase == Fase.PERSECUCION && hastaDisparo <= ANTICIPACION) {
                lote.setColor(1f, 0.68f, 0.12f, 1f);
                lote.draw(pixel, posicionX + ANCHO * 0.70f, posicionY + ALTO * 0.64f, 10f, 8f);
            }
        }
        for (Bala bala : balas) {
            if (bala.carril != carril) continue;
            Rectangle zona = bala.zona;
            lote.setColor(1f, 0.4f, 0.08f, 1f);
            lote.draw(pixel, zona.x, zona.y, zona.width, zona.height);
            lote.setColor(1f, 1f, 0.7f, 1f);
            lote.draw(pixel, zona.x + 6f, zona.y + 1f, 12f, 4f);
        }
        lote.setPackedColor(colorAnterior);
    }

    void dibujarAlerta(SpriteBatch lote, float altoMundo) {
        float anterior = lote.getPackedColor();
        float mezcla = (1f + MathUtils.cos(tiempo * MathUtils.PI2)) / 2f;
        lote.setColor(mezcla, 0.05f, 1f - mezcla, 0.24f);
        lote.draw(pixel, 0, 0, anchoMundo, altoMundo);
        lote.setColor(0.04f, 0.04f, 0.06f, 0.85f);
        lote.draw(pixel, (anchoMundo - 440f) / 2f, (altoMundo - 90f) / 2f, 440f, 90f);
        lote.setPackedColor(anterior);
    }

    void detenerAudio() {
        sonidoDisparo.stop();
        sirena.stop();
        musica.stop();
    }

    void reiniciar() {
        detenerAudio();
        fase = Fase.AUSENTE;
        siguienteHito = 1000;
        objetivo = null;
        balas.clear();
    }

    void liberar() {
        sonidoDisparo.dispose();
        coche.dispose();
        policia.dispose();
        pixel.dispose();
        sirena.dispose();
        musica.dispose();
    }
}
