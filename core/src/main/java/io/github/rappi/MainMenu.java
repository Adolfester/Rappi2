package io.github.rappi;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;

 public class MainMenu implements Screen {

     private final Main game;
     private SpriteBatch batch;
     private BitmapFont font;
     private GlyphLayout layout;
     private OrthographicCamera camera;
     private Viewport viewport;
     private ShapeRenderer shapeRenderer;

     private Rectangle botonJugar;
     private Rectangle botonPersonaje;
     private Vector3 touchPoint;
     private Texture logo;
     private Music menuMusic;
     private Sound clickSound;

     public MainMenu(Main game) {
         this.game = game;
         logo = new Texture(Gdx.files.internal("rappi.png"));
     }

     @Override
     public void show() {
         batch = new SpriteBatch();
         font = new BitmapFont();
         shapeRenderer = new ShapeRenderer();
         touchPoint = new Vector3();

         botonJugar = new Rectangle(0, 0, 200, 50);
         botonPersonaje = new Rectangle(0, 0, 200, 50);

         layout = new GlyphLayout();
         camera = new OrthographicCamera();
         viewport = new ScreenViewport(camera);

         viewport.update(
             Gdx.graphics.getWidth(),
             Gdx.graphics.getHeight(),
             true
         );

         // Cargar audio con resguardo si el archivo no existe aún
         if (Gdx.files.internal("click.wav").exists()) {
             clickSound = Gdx.audio.newSound(Gdx.files.internal("click.wav"));
         }

         if (Gdx.files.internal("menu_music.mp3").exists()) {
             menuMusic = Gdx.audio.newMusic(Gdx.files.internal("menu_music.mp3"));
             menuMusic.setLooping(true);
             menuMusic.setVolume(0.5f);
             menuMusic.play();
         }
     }



     @Override
     public void render(float delta) {
         if (Gdx.input.justTouched()) {
             touchPoint.set(
                 Gdx.input.getX(),
                 Gdx.input.getY(),
                 0
             );

             viewport.unproject(touchPoint);

             if (botonJugar.contains(touchPoint.x, touchPoint.y)) {
                 if (clickSound != null) clickSound.play();
                 game.setScreen(new GameScreen(game));
                 return;
             }

             if (botonPersonaje.contains(touchPoint.x, touchPoint.y)) {
                 if (clickSound != null) clickSound.play();
                 game.setScreen(new CharacterSelectScreen(game));
                 return;
             }
         }

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);

        float x = (viewport.getWorldWidth() - layout.width) / 2f;
        float y = (viewport.getWorldHeight() + layout.height) / 2f;

        float logox = (viewport.getWorldWidth() - 200) / 2f;
        float logoy = (viewport.getWorldHeight() + 100) / 2f;

        float botonX = (viewport.getWorldWidth() - botonJugar.width) / 2f;
        float botonJugarY = viewport.getWorldHeight() / 2f - 40;
        float botonPersonajeY = botonJugarY - 65;

        botonJugar.setPosition(botonX, botonJugarY);
        botonPersonaje.setPosition(botonX, botonPersonajeY);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Botón Jugar (Verde)
        shapeRenderer.setColor(0.2f, 0.7f, 0.3f, 1);
        shapeRenderer.rect(botonJugar.x, botonJugar.y, botonJugar.width, botonJugar.height);

        // Botón Personaje (Naranja)
        shapeRenderer.setColor(0.9f, 0.5f, 0.2f, 1);
        shapeRenderer.rect(botonPersonaje.x, botonPersonaje.y, botonPersonaje.width, botonPersonaje.height);

        shapeRenderer.end();

        GlyphLayout layoutBotonJugar = new GlyphLayout(font, "JUGAR");
        GlyphLayout layoutBotonPersonaje = new GlyphLayout(font, "PERSONAJE");

        float textoJugarX = botonJugar.x + (botonJugar.width - layoutBotonJugar.width) / 2f;
        float textoJugarY = botonJugar.y + (botonJugar.height + layoutBotonJugar.height) / 2f;

        float textoPersonajeX = botonPersonaje.x + (botonPersonaje.width - layoutBotonPersonaje.width) / 2f;
        float textoPersonajeY = botonPersonaje.y + (botonPersonaje.height + layoutBotonPersonaje.height) / 2f;

        batch.begin();

        font.draw(batch, layout, x, y);
        batch.draw(logo, logox, logoy, 200, 100);

        font.draw(batch, layoutBotonJugar, textoJugarX, textoJugarY);
        font.draw(batch, layoutBotonPersonaje, textoPersonajeX, textoPersonajeY);

        batch.end();
    }

     @Override
     public void resize(int width, int height) {
         viewport.update(width, height, true);

         camera.update();
    }

     @Override
     public void pause() {

     }

     @Override
     public void resume() {

     }

     @Override
     public void hide() {
         if (menuMusic != null) {
             menuMusic.stop();
         }
     }

     @Override
     public void dispose() {
         batch.dispose();
         font.dispose();
         shapeRenderer.dispose();
         logo.dispose();
         if (clickSound != null) clickSound.dispose();
         if (menuMusic != null) menuMusic.dispose();
     }


}
