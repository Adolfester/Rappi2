package io.github.rappi;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Piso {

    private Texture sueloTexture;
    private float offsetSueloX;
    private float velocidad;

    private float yBase;        // Posición Y de la esquina inferior del suelo en pantalla
    private float altoSuelo;    // Alto total dibujado en pantalla
    private float alturaCarril; // Alto individual de cada uno de los 3 carriles

    public Piso(float yBase, float escala) {
        sueloTexture = new Texture(Gdx.files.internal("la66.png"));
        sueloTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.ClampToEdge);
        sueloTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        this.yBase = yBase;
        // 23px de alto original * escala (ej: 3x = 69px)
        this.altoSuelo = sueloTexture.getHeight() * escala;
        this.alturaCarril = this.altoSuelo / 3f;

        this.offsetSueloX = 0;
        this.velocidad = 300f;
    }

    public void update(float delta) {
        offsetSueloX += velocidad * delta;
    }

    public void draw(SpriteBatch batch) {
        batch.draw(
            sueloTexture,
            0, yBase,                                       // Dibujar desde yBase
            Gdx.graphics.getWidth(), altoSuelo,             // Ocupa todo el ancho
            (int) offsetSueloX, 0,                          // Recorte horizontal continuo
            sueloTexture.getWidth(), sueloTexture.getHeight(),
            false, false
        );
    }

    // Devuelve la posición Y exacta donde pisa el jugador según el carril (0, 1 o 2)
    public float getPosYCarril(int carril) {
        return yBase + (carril * alturaCarril);
    }

    public void dispose() {
        sueloTexture.dispose();
    }
}
