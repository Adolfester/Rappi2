package io.github.rappi;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class Obstaculos {

    private Texture texture;
    private float x, y;
    private float width = 80f;
    private float height = 70f;
    private Rectangle bounds;
    private int carril;
    private int tipo;
    private float offsetX = 15f;
    private float offsetY = 8f;

    public Obstaculos(Piso piso, int carrilAsignado, float worldWidth) {
        this.carril = carrilAsignado;
        this.y = piso.getPosYCarril(carril);
        this.x = worldWidth; // Aparece al borde derecho de la pantalla

        // Seleccionar aleatoriamente uno de los 5 obstáculos (0 al 4)
        this.tipo = MathUtils.random(0, 4);
        String texturePath = "";
        float reductionW = 30f;
        float reductionH = 15f;

        // Asignar archivo y dimensiones proporcionales con hitboxes tolerantes
        switch (tipo) {
            case 0:
                texturePath = "Obstaculos/cono.png";
                width = 55f;
                height = 65f;
                offsetX = 12f;
                reductionW = 24f;
                break;
            case 1:
                texturePath = "Obstaculos/auto_blanco.png";
                width = 170f;  // Los autos tienen hitbox más corta al centro
                height = 70f;
                offsetX = 35f;
                reductionW = 70f;
                break;
            case 2:
                texturePath = "Obstaculos/auto_negro.png";
                width = 170f;
                height = 70f;
                offsetX = 35f;
                reductionW = 70f;
                break;
            case 3:
                texturePath = "Obstaculos/auto_rojo.png";
                width = 170f;
                height = 70f;
                offsetX = 35f;
                reductionW = 70f;
                break;
            case 4:
                texturePath = "Obstaculos/basura.png";
                width = 85f;
                height = 70f;
                offsetX = 18f;
                reductionW = 36f;
                break;
        }

        if (Gdx.files.internal(texturePath).exists()) {
            texture = new Texture(Gdx.files.internal(texturePath));
        } else {
            texture = new Texture(Gdx.files.internal("Fondo/rappi.png"));
        }

        // Hitbox ajustada para dar más margen y tiempo de reacción
        bounds = new Rectangle(x + offsetX, y + offsetY, width - reductionW, height - reductionH);
    }

    public void update(float delta, float velocidadCarretera) {
        x -= velocidadCarretera * delta;
        bounds.setPosition(x + offsetX, y + offsetY);
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texture, x, y, width, height);
    }

    public boolean estaFueraDePantalla() {
        return x + width < 0;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public int getCarril() {
        return carril;
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }
}
