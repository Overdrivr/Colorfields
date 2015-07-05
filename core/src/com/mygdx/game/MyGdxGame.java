package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.overdrivr.model.GameEngine;
import java.util.Locale;


public class MyGdxGame extends Game {
    public SpriteBatch batch;
    public BitmapFont font;
    public Locale locale;
    public GameEngine engine;

    @Override
    public void create () {

        batch = new SpriteBatch();

        // Fonts
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto_v1.2/RobotoCondensed/RobotoCondensed-Light.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 80;
        font = generator.generateFont(parameter); // font size 12 pixels
        generator.dispose();

        //Internalization (English default for now)
        locale = new Locale("en", "GB");

        //Init game engine
        engine = new GameEngine();

        this.setScreen(new GameScreen(this));
    }

    @Override
    public void render () {
        super.render();
    }

    public void dispose(){
        engine.dispose();
        batch.dispose();
        font.dispose();
    }
}
