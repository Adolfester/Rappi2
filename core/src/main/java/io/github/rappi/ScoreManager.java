package io.github.rappi;

public class ScoreManager {

    private float score;
    private int scoreEntero;

    public ScoreManager() {
        this.score = 0f;
        this.scoreEntero = 0;
    }

    public void update(float delta) {
        // Aumenta el puntaje progresivamente según el tiempo transcurrido
        score += 10f * delta;
        scoreEntero = (int) score;
    }

    public int getScore() {
        return scoreEntero;
    }

    // Devuelve el puntaje formateado a 5 dígitos con ceros a la izquierda (ej: "00042")
    public String getScoreFormateado() {
        return String.format("%01d", scoreEntero);
    }

    public void reset() {
        score = 0f;
        scoreEntero = 0;
    }
}
