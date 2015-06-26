package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

import java.io.File;

public class MyGdxGame extends Game {
    public SpriteBatch batch;
    public BitmapFont font;


    @Override
    public void create () {

        batch = new SpriteBatch();
        //USE ROBOTO INSTEAD
        //font = new BitmapFont();
        //font.setScale(3.f);
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto_v1.2/RobotoCondensed/RobotoCondensed-Light.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 80;
        font = generator.generateFont(parameter); // font size 12 pixels
        generator.dispose(); // don't forget to dispose to avoid memory leaks!

        // Load file levels
        // TODO Must be moved to the StartupScreen
        FileHandle leveldata = Gdx.files.internal("Levels/level_111.xml");
        FileHandle globaldata = Gdx.files.internal("Values/global_constants.properties");
        this.setScreen(new GameScreen(this,globaldata,leveldata));
    }

    @Override
    public void render () {
        super.render();
    }

    public void dispose(){
        batch.dispose();
        font.dispose();
    }
}
