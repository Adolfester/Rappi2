package io.github.rappi;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Piso {

    private Texture texturaSuelo;
    private float desplazamientoSueloX;

    private float yBase;
    private float altoSuelo;
    private float alturaCarril;

    public Piso(float yBase, float escala) {
        texturaSuelo = new Texture(Gdx.files.internal("Fondo/la66.png"));
        texturaSuelo.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.ClampToEdge);
        texturaSuelo.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        this.yBase = yBase;
        this.altoSuelo = texturaSuelo.getHeight() * escala;
        this.alturaCarril = this.altoSuelo / 3f;

    }

    public void reset() {
        desplazamientoSueloX = 0f;
    }

    public void avanzar(float avance, float anchoMundo) {
        desplazamientoSueloX = (desplazamientoSueloX + avance) % anchoMundo;
    }

    public void draw(SpriteBatch loteDibujo, float anchoMundo) {
        float u = desplazamientoSueloX / anchoMundo;
        loteDibujo.draw(texturaSuelo, 0f, yBase, anchoMundo, altoSuelo, u, 1f, u + 1f, 0f);
    }

    public float getPosYCarril(int carril) {
        return yBase + (carril * alturaCarril);
    }

    public void dispose() {
        texturaSuelo.dispose();
    }
}
