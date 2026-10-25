package io.github.rappi;

public class ScoreManager {

    private float score;
    private int scoreEntero;

    // Variables para el control de velocidad dinámica
    private float currentSpeed;
    private float baseSpeed;
    private float accelerationRate;
    private float maxSpeed; // Límite máximo de velocidad

    public ScoreManager(float initialSpeed) {
        this.score = 0f;
        this.scoreEntero = 0;
        this.baseSpeed = initialSpeed;
        this.currentSpeed = initialSpeed;
        this.accelerationRate = 10f; // Ajusta este valor si quieres que acelere más rápido o más lento
        this.maxSpeed = 650.0f;       // Tope máximo de velocidad (puedes ajustarlo si prefieres más o menos)
    }

    public void update(float delta) {
        // Se corrige el operador de multiplicación y se escala con la velocidad actual
        score += delta * 10f * (currentSpeed / baseSpeed);
        scoreEntero = (int) score;

        // La velocidad aumenta gradualmente con el paso del tiempo
        if (currentSpeed < maxSpeed) {
            currentSpeed += accelerationRate * delta;
            if (currentSpeed > maxSpeed) {
                currentSpeed = maxSpeed;
            }
        }
    }

    public int getScore() {
        return scoreEntero;
    }

    public float getVelocidadActual() {
        return currentSpeed;
    }

    // Devuelve el puntaje formateado a 5 dígitos con ceros a la izquierda (ej: "00042")
    public String getScoreFormateado() {
        return String.format("%01d", scoreEntero);
    }

    public void reset() {
        score = 0f;
        scoreEntero = 0;
        currentSpeed = baseSpeed;
    }
}
