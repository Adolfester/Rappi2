package io.github.rappi;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;

public class Main extends Game {

    private Preferences opciones;

    boolean permiteCambiarVentana() {
        return Gdx.app.getType() == ApplicationType.Desktop;
    }

    boolean cambiarVentana(int ancho, int alto) {
        if (!permiteCambiarVentana() || !Gdx.graphics.setWindowedMode(ancho, alto)) return false;
        opciones.putInteger("ancho", ancho);
        opciones.putInteger("alto", alto);
        opciones.flush();
        return true;
    }

    private int personajeSeleccionado = 0;

    @Override
    public void create() {
        opciones = Gdx.app.getPreferences("JuegoRappiVentana");
        int ancho = opciones.getInteger("ancho", 0);
        int alto = opciones.getInteger("alto", 0);
        if (permiteCambiarVentana() && ancho > 0 && alto > 0) {
            Gdx.graphics.setWindowedMode(ancho, alto);
        }
        setScreen(new MainMenu(this));
    }

    @Override
    public void setScreen(Screen siguiente) {
        Screen anterior = getScreen();
        if (anterior == siguiente) return;
        super.setScreen(siguiente);
        if (anterior != null) anterior.dispose();
    }

    @Override
    public void dispose() {
        setScreen(null);
    }

    public int getSelectedCharacter() {
        return personajeSeleccionado;
    }

    public void setSelectedCharacter(int personajeSeleccionado) {
        this.personajeSeleccionado = personajeSeleccionado;
    }
}
