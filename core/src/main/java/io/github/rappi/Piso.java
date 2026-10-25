package io.github.rappi;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Piso {

    private Texture sueloTexture;
    private float offsetSueloX;
    private float velocidad;

    private float yBase;
    private float altoSuelo;
    private float alturaCarril;

    public Piso(float yBase, float escala) {
        sueloTexture = new Texture(Gdx.files.internal("la66.png"));
        sueloTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.ClampToEdge);
        sueloTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        this.yBase = yBase;
        this.altoSuelo = sueloTexture.getHeight() * escala;
        this.alturaCarril = this.altoSuelo / 3f;

        this.offsetSueloX = 0;
        this.velocidad = 300f;
    }

    public void update(float delta) {
        offsetSueloX += velocidad * delta;
    }

    // Ahora recibe el ancho del mundo virtual (worldWidth)
    public void draw(SpriteBatch batch, float worldWidth) {
        batch.draw(
            sueloTexture,
            0, yBase,
            worldWidth, altoSuelo,             // Usa el ancho del mundo virtual
            (int) offsetSueloX, 0,
            sueloTexture.getWidth(), sueloTexture.getHeight(),
            false, false
        );
    }

    public void setVelocidad(float velocidad) {
        this.velocidad = velocidad;
    }

    public float getPosYCarril(int carril) {
        return yBase + (carril * alturaCarril);
    }

    public void dispose() {
        sueloTexture.dispose();
    }
}
