package io.github.rappi;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class Obstaculos {

    private static final float ESCALA_OBSTACULOS = 1.10f;

    private final Texture textura;
    private float x, y;
    private float ancho = 80f;
    private float alto = 70f;
    private final Rectangle zonaColision;
    private final int carril;
    private final boolean esAuto;
    private final Ruedas ruedas;
    private float anguloRuedas;

    // Centros medidos desde la esquina superior izquierda de los PNG de 1643 x 640.
    private static final Ruedas[] RUEDAS = {
        null, new Ruedas(402, 1296, 462, 80, 112, true),
        new Ruedas(289, 1306, 465, 77, 106, true),
        new Ruedas(393, 1257, 435, 84, 112, false), null
    };

    private static class Ruedas {
        final int izquierda, derecha, centroY, radioLlanta, radioNeumatico;
        final boolean tieneTaza;
        Ruedas(int izquierda, int derecha, int centroY, int radioLlanta,
                int radioNeumatico, boolean tieneTaza) {
            this.izquierda = izquierda;
            this.derecha = derecha;
            this.centroY = centroY;
            this.radioLlanta = radioLlanta;
            this.radioNeumatico = radioNeumatico;
            this.tieneTaza = tieneTaza;
        }
    }

    // SpriteBatch recibe cuatro vértices con posición, color y coordenadas de textura.
    private static final int SECTORES = 20;
    private static final float[] COSENOS = new float[SECTORES + 1];
    private static final float[] SENOS = new float[SECTORES + 1];
    static {
        for (int sector = 0; sector <= SECTORES; sector++) {
            float angulo = MathUtils.PI2 * sector / SECTORES;
            COSENOS[sector] = MathUtils.cos(angulo);
            SENOS[sector] = MathUtils.sin(angulo);
        }
    }

    private final float[] verticesRueda;
    private float bordeTraseroAnterior;
    private static final String[] ARCHIVOS = {
        "cono.png", "auto_blanco.png", "auto_negro.png", "auto_rojo.png", "basura.png"
    };

    static Texture[] cargarTexturas() {
        Texture[] texturas = new Texture[ARCHIVOS.length];
        for (int i = 0; i < ARCHIVOS.length; i++) {
            String ruta = "Obstaculos/" + ARCHIVOS[i];
            if (!Gdx.files.internal(ruta).exists()) ruta = "Fondo/rappi.png";
            texturas[i] = new Texture(Gdx.files.internal(ruta));
        }
        return texturas;
    }
    private float margenX = 15f;

    public Obstaculos(Piso piso, int carrilAsignado, float anchoMundo, int tipo, Texture textura) {
        this.carril = carrilAsignado;
        esAuto = tipo >= 1 && tipo <= 3;
        ruedas = textura.getWidth() == 1643 && textura.getHeight() == 640 ? RUEDAS[tipo] : null;
        verticesRueda = ruedas == null ? null : new float[20];
        this.y = piso.getPosYCarril(carril);
        this.x = anchoMundo; // Aparece al borde derecho de la pantalla

        this.textura = textura;

        float margenY = 8f;
        float reduccionAncho = 30f;
        float reduccionAlto = 15f;

        // Asignar archivo y dimensiones proporcionales con hitboxes tolerantes
        switch (tipo) {
            case 0:
                ancho = 55f;
                alto = 65f;
                margenX = 12f;
                reduccionAncho = 24f;
                break;
            case 1:
            case 2:
            case 3:
                ancho = 170f;
                alto = 70f;
                margenX = 35f;
                reduccionAncho = 70f;
                break;
            case 4:
                ancho = 98f;
                alto = 81f;
                margenX = 21f;
                margenY = 9f;
                reduccionAncho = 42f;
                reduccionAlto = 17f;
                break;
        }

        ancho *= ESCALA_OBSTACULOS;
        alto *= ESCALA_OBSTACULOS;
        margenX *= ESCALA_OBSTACULOS;
        margenY *= ESCALA_OBSTACULOS;
        reduccionAncho *= ESCALA_OBSTACULOS;
        reduccionAlto *= ESCALA_OBSTACULOS;

        // Hitbox ajustada para dar más margen y tiempo de reacción
        zonaColision = new Rectangle(x + margenX, y + margenY, ancho - reduccionAncho, alto - reduccionAlto);
    }

    public void avanzar(float avance) {
        bordeTraseroAnterior = zonaColision.x + zonaColision.width;
        x -= avance;
        if (ruedas != null) {
            float radio = ruedas.radioNeumatico * ancho / textura.getWidth();
            // Giro visual reducido para distinguir los rayos a la velocidad del runner.
            anguloRuedas = (anguloRuedas + 0.18f * avance / radio) % MathUtils.PI2;
        }
        zonaColision.x = x + margenX;
    }

    // El borde trasero cruza la posición del jugador una sola vez por auto.
    public boolean acabaDePasarCerca(float xJugador, int carrilJugador) {
        float bordeTrasero = zonaColision.x + zonaColision.width;
        return esAuto && Math.abs(carril - carrilJugador) <= 1
                && bordeTrasero < xJugador && bordeTraseroAnterior >= xJugador;
    }

    public void draw(SpriteBatch loteDibujo) {
        loteDibujo.draw(textura, x, y, ancho, alto);
        if (ruedas != null) {
            float coseno = MathUtils.cos(anguloRuedas);
            float seno = MathUtils.sin(anguloRuedas);
            dibujarLlanta(loteDibujo, ruedas.izquierda, coseno, seno);
            dibujarLlanta(loteDibujo, ruedas.derecha, coseno, seno);
        }
    }

    private void dibujarLlanta(SpriteBatch loteDibujo, int ejeX, float coseno, float seno) {
        float color = loteDibujo.getPackedColor();
        ponerVertice(0, ejeX, 0f, 0f, 0f, 0f, color);
        ponerVertice(3, ejeX, 0f, 0f, 0f, 0f, color);
        // El disco dibuja la misma llanta sobre el sprite, con la textura girada.
        for (int i = 0; i <= SECTORES; i++) {
            float horizontal = ruedas.radioLlanta * COSENOS[i];
            float vertical = ruedas.radioLlanta * SENOS[i];
            ponerVertice(2, ejeX, horizontal, vertical,
                    horizontal * coseno + vertical * seno,
                    vertical * coseno - horizontal * seno, color);
            if (i > 0) loteDibujo.draw(textura, verticesRueda, 0, verticesRueda.length);
            System.arraycopy(verticesRueda, 10, verticesRueda, 5, 5);
        }
        if (ruedas.tieneTaza) {
            // Una marca oscura permite ver el giro en las tazas lisas.
            float centroX = x + (ejeX + 33f * coseno) * ancho / textura.getWidth();
            float centroY = y + alto * (1f - (ruedas.centroY - 33f * seno) / textura.getHeight());
            float ladoMarca = 1.8f * ESCALA_OBSTACULOS;
            loteDibujo.draw(textura, centroX - ladoMarca / 2f, centroY - ladoMarca / 2f, ladoMarca, ladoMarca,
                    ejeX + 12, ruedas.centroY, 1, 1, false, false);
        }
    }

    private void ponerVertice(int indice, int ejeX, float desplazamientoX, float desplazamientoY, float texturaX, float texturaY, float color) {
        int i = indice * 5;
        verticesRueda[i] = x + (ejeX + desplazamientoX) * ancho / textura.getWidth();
        verticesRueda[i + 1] = y + alto * (1f - (ruedas.centroY - desplazamientoY) / textura.getHeight());
        verticesRueda[i + 2] = color;
        verticesRueda[i + 3] = (ejeX + texturaX) / textura.getWidth();
        verticesRueda[i + 4] = (ruedas.centroY - texturaY) / textura.getHeight();
    }

    public boolean estaFueraDePantalla() {
        return x + ancho < 0;
    }

    public Rectangle getBounds() {
        return zonaColision;
    }

    public float getAncho() {
        return ancho;
    }

    public int getCarril() {
        return carril;
    }

}
