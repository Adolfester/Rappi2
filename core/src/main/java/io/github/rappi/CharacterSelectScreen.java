package io.github.rappi;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class CharacterSelectScreen implements Screen {

    private final Main game;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private GlyphLayout layout;

    private OrthographicCamera camera;
    private Viewport viewport;
    private Vector3 touchPoint;

    private final int NUM_CHARACTERS = 4;
    private Texture[] characterTextures;
    private Rectangle[] characterBoxes;
    private Rectangle btnVolver;

    public CharacterSelectScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        if (Gdx.files.internal("8bitOperatorPlus-Regular.fnt").exists()) {
            font = new BitmapFont(Gdx.files.internal("8bitOperatorPlus-Regular.fnt"));
        } else {
            font = new BitmapFont();
        }
        layout = new GlyphLayout();
        touchPoint = new Vector3();

        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);
        viewport.apply();
        camera.position.set(1280 / 2f, 720 / 2f, 0);

        characterTextures = new Texture[NUM_CHARACTERS];
        characterBoxes = new Rectangle[NUM_CHARACTERS];

        for (int i = 0; i < NUM_CHARACTERS; i++) {
            String fileName = "RepartidorSprites/jugador_suelo_" + i + ".png";
            if (Gdx.files.internal(fileName).exists()) {
                characterTextures[i] = new Texture(Gdx.files.internal(fileName));
            } else {
                characterTextures[i] = new Texture(Gdx.files.internal("RepartidorSprites/jugador_suelo.png"));
            }
        }

        float boxWidth = 140f;
        float boxHeight = 140f;
        float spacing = 30f;
        float totalWidth = (NUM_CHARACTERS * boxWidth) + ((NUM_CHARACTERS - 1) * spacing);
        float startX = (1280f - totalWidth) / 2f;
        float startY = (720f - boxHeight) / 2f;

        for (int i = 0; i < NUM_CHARACTERS; i++) {
            characterBoxes[i] = new Rectangle(startX + i * (boxWidth + spacing), startY, boxWidth, boxHeight);
        }

        btnVolver = new Rectangle((1280f - 200f) / 2f, 100f, 200f, 50f);
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.justTouched()) {
            touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(touchPoint);

            for (int i = 0; i < NUM_CHARACTERS; i++) {
                if (characterBoxes[i].contains(touchPoint.x, touchPoint.y)) {
                    game.setSelectedCharacter(i);
                    break;
                }
            }

            if (btnVolver.contains(touchPoint.x, touchPoint.y)) {
                game.setScreen(new MainMenu(game));
                return;
            }
        }

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < NUM_CHARACTERS; i++) {
            if (game.getSelectedCharacter() == i) {
                shapeRenderer.setColor(0.9f, 0.8f, 0.2f, 1);
            } else {
                shapeRenderer.setColor(0.2f, 0.25f, 0.35f, 1);
            }
            shapeRenderer.rect(characterBoxes[i].x, characterBoxes[i].y, characterBoxes[i].width, characterBoxes[i].height);
        }

        shapeRenderer.setColor(0.8f, 0.3f, 0.3f, 1);
        shapeRenderer.rect(btnVolver.x, btnVolver.y, btnVolver.width, btnVolver.height);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(3);
        shapeRenderer.setColor(Color.WHITE);
        for (int i = 0; i < NUM_CHARACTERS; i++) {
            shapeRenderer.rect(characterBoxes[i].x + 5, characterBoxes[i].y + 5, characterBoxes[i].width - 10, characterBoxes[i].height - 10);
        }
        shapeRenderer.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        layout.setText(font, "SELECCIONA TU PERSONAJE");
        font.draw(batch, layout, (1280f - layout.width) / 2f, 600f);

        for (int i = 0; i < NUM_CHARACTERS; i++) {
            float imgWidth = 80f;
            float imgHeight = 80f;
            float imgX = characterBoxes[i].x + (characterBoxes[i].width - imgWidth) / 2f;
            float imgY = characterBoxes[i].y + (characterBoxes[i].height - imgHeight) / 2f + 10f;

            batch.draw(characterTextures[i], imgX, imgY, imgWidth, imgHeight);

            String label = "P" + (i + 1);
            layout.setText(font, label);
            font.draw(batch, label, characterBoxes[i].x + (characterBoxes[i].width - layout.width) / 2f, characterBoxes[i].y + 25f);
        }

        layout.setText(font, "VOLVER");
        font.draw(batch, layout, btnVolver.x + (btnVolver.width - layout.width) / 2f, btnVolver.y + (btnVolver.height + layout.height) / 2f);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        for (Texture texture : characterTextures) {
            if (texture != null) texture.dispose();
        }
    }
}
