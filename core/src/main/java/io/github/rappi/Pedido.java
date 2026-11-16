package io.github.rappi;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

class Pedido {
    final int carril;
    final Rectangle zonaColision;

    Pedido(Piso piso, int carril, float x) {
        this.carril = carril;
        zonaColision = new Rectangle(x, piso.getPosYCarril(carril) + 16f, 42f, 36f);
    }

    void draw(SpriteBatch loteDibujo, Texture textura) {
        loteDibujo.draw(textura, zonaColision.x, zonaColision.y, zonaColision.width, zonaColision.height);
    }
}
