package io.github.rappi;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ParallaxLayer {
    private Texture textura;
    private float factorVelocidad;
    private float desplazamientoX = 0f;

    public ParallaxLayer(String rutaArchivo, float factorVelocidad) {
        this.textura = new Texture(Gdx.files.internal(rutaArchivo));
        this.textura.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        this.textura.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        this.factorVelocidad = factorVelocidad;
    }

    // Dibujado actualizado con soporte de tinte de color (Día / Tarde / Noche)
    public void updateAndDraw(SpriteBatch loteDibujo, float avance, float anchoPantalla, float altoPantalla, Color tinte) {
        desplazamientoX = (desplazamientoX + avance * factorVelocidad) % anchoPantalla;

        // Guarda el color previo del batch y aplica el nuevo tinte
        float colorAnterior = loteDibujo.getPackedColor();
        loteDibujo.setColor(tinte);

        float u = desplazamientoX / anchoPantalla;
        loteDibujo.draw(textura, 0f, 0f, anchoPantalla, altoPantalla, u, 1f, u + 1f, 0f);

        // Restaura el color original del batch para no teñir al jugador, piso u otros sprites
        loteDibujo.setPackedColor(colorAnterior);
    }

    public void reset() {
        desplazamientoX = 0f;
    }

    public void dispose() {
        textura.dispose();
    }
}
