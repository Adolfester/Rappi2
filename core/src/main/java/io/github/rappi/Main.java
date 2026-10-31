package io.github.rappi;

import com.badlogic.gdx.Game;

public class Main extends Game {

    private int selectedCharacter = 0;

    @Override
    public void create() {
        setScreen(new MainMenu(this));
    }

    public int getSelectedCharacter() {
        return selectedCharacter;
    }

    public void setSelectedCharacter(int selectedCharacter) {
        this.selectedCharacter = selectedCharacter;
    }
}
