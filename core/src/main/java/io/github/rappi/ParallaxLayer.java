package io.github.rappi;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ParallaxLayer {
    private Texture texture;
    private float speedFactor;
    private float currentX = 0f;

    public ParallaxLayer(String filePath, float speedFactor) {
        this.texture = new Texture(Gdx.files.internal(filePath));
        this.texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        this.texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        this.speedFactor = speedFactor;
    }

    public void updateAndDraw(SpriteBatch batch, float baseSpeed, float delta, float screenWidth, float screenHeight) {
        currentX += (baseSpeed * speedFactor) * delta;

        batch.draw(
            texture,
            0, 0,                                // Posición (X, Y) en pantalla
            screenWidth, screenHeight,            // Tamaño al que se escala en pantalla
            (int) currentX, 0,                    // Origen recortado (srcX desfasado)
            texture.getWidth(), texture.getHeight(),
            false, false
        );
    }

    public void dispose() {
        texture.dispose();
    }
}
