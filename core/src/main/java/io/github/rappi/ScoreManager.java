package io.github.rappi;

public class ScoreManager {
    // Ritmo del Dino clásico en una pista de 600 unidades, a 60 Hz.
    private static final double INICIAL = 360.0;
    private static final double MAXIMA = 780.0;
    private static final double ACELERACION = 3.6;
    private final float escala;
    private double velocidad = INICIAL;
    private double distancia;
    private int bonificacion;

    public ScoreManager(float anchoMundo) {
        escala = anchoMundo / 600f;
    }

    // Devuelve el avance de este fotograma en unidades del mundo de Rappi.
    public float update(float segundosTranscurridos) {
        double acelerando = Math.min(segundosTranscurridos, (MAXIMA - velocidad) / ACELERACION);
        double avance = velocidad * acelerando + 0.5 * ACELERACION * acelerando * acelerando
                + MAXIMA * (segundosTranscurridos - acelerando);
        velocidad = Math.min(MAXIMA, velocidad + ACELERACION * acelerando);
        distancia += avance;
        return (float) (avance * escala);
    }

    public float getVelocidadActual() {
        return (float) velocidad * escala;
    }

    public float distanciaEntreObstaculos(float ancho, float variacion) {
        float velocidadPorFotograma = (float) velocidad / 60f;
        float huecoMinimo = ancho * velocidadPorFotograma + 120f * 0.6f * escala;
        return ancho + huecoMinimo * variacion;
    }

    public void sumarPedido() {
        bonificacion += 25;
    }

    public int getPuntosPorDistancia() {
        return (int) Math.round(distancia * 0.025);
    }

    public int getScore() {
        return getPuntosPorDistancia() + bonificacion;
    }

    public String getScoreFormateado() {
        return String.format("%05d", getScore());
    }

    public void reset() {
        velocidad = INICIAL;
        distancia = 0;
        bonificacion = 0;
    }
}
