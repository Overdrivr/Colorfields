package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.I18NBundle;

import java.util.Locale;
import java.util.Random;

/**
 * Created by Bart on 26/04/2015.
 */
public class GameScreen implements Screen {

    final MyGdxGame game;
    OrthographicCamera camera;
    final float camerabase = 800;
    float cameraunitsx;
    float cameraunitsy;
    Vector2 cameraSpeed;

    //EFFECTS
    private ParticleEffect effect;

    //Game textures
    private Texture[] asteroids;

    Random rnd;

    public I18NBundle myBundle;

    Stage stage;
    Skin skin;
    // For debug drawing
    private ShapeRenderer shapeRenderer;

    public GameScreen(final MyGdxGame g) {
        game = g;
        initSkin();

        //Internalization
        FileHandle baseFileHandle = Gdx.files.internal("I18N/GameScreenBundle");
        Locale locale = new Locale("en", "GB");
        myBundle = I18NBundle.createBundle(baseFileHandle, locale);

        //Camera
        camera = new OrthographicCamera();
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        cameraunitsx = camerabase;
        cameraunitsy = camerabase * h / w;
        camera.setToOrtho(false, cameraunitsx, cameraunitsy);
        cameraSpeed = new Vector2(0,0);
        Gdx.app.log("Camera","Dimensions("+ Float.toString(cameraunitsx)+","+ Float.toString(cameraunitsy) + ")");

        //INPUT HANDLING
        MyGestureListener gestureListener = new MyGestureListener();
        gestureListener.setGamescreen(this);
        Gdx.input.setInputProcessor(new GestureDetector(gestureListener));

        //Generators
        rnd = new Random();
        rnd.setSeed(0);

        //TEXTURES
        loadImages();

        //PARTICLE EFFECTS
        effect = new ParticleEffect();
        effect.load(Gdx.files.internal("Particles/green_peaceful_flame"),Gdx.files.internal("Particles"));
        effect.setPosition(300,300);
        effect.findEmitter("Fire").setContinuous(true);
        effect.start();

        //UI Init
        stage = new Stage();
        shapeRenderer = new ShapeRenderer();
        //Gdx.input.setInputProcessor(stage);

    }



    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //Update camera position & velocity for swipe gesture
        camera.translate(cameraSpeed.x*delta,cameraSpeed.y*delta);
        final float alpha = 0.91f;
        cameraSpeed.x *= alpha;
        cameraSpeed.y *= alpha;

        if(cameraSpeed.len() < 0.001f)
        {
            cameraSpeed.x = 0;
            cameraSpeed.y = 0;
        }

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        //Update particles
        effect.update(delta);

        game.batch.begin();
        game.batch.draw(asteroids[0], 200, 200);
        effect.draw(game.batch);
        //stage.act(Gdx.graphics.getDeltaTime());
        //stage.draw();
        // This is optional, but enables debug lines for tables.
        //table.drawDebug(shapeRenderer);
        game.batch.end();
    }

    @Override
    public void resize(int x, int y){
        stage.getViewport().update(x, y, true);
    }

    @Override
    public void show(){
        //Nothing to do at screen startup
    }

    @Override
    public void hide(){
        //Nothing to do at screen stop
    }

    @Override
    public void pause(){
        //Nothing to do at screen pause
    }

    @Override
    public void resume(){
        //Nothing to do at screen resume
    }

    @Override
    public void dispose(){
        stage.dispose();
        shapeRenderer.dispose();
        asteroids[0].dispose();
    }

    public void RawPanView(float deltaX, float deltaY)
    {
        float dx = deltaX/Gdx.graphics.getWidth()*cameraunitsx;
        float dy = deltaY/Gdx.graphics.getHeight()*cameraunitsy;
        dx *= camera.zoom;
        dy *= camera.zoom;
        camera.translate(dx,dy);
    }

    public void Fling(float velocityX, float velocityY){
        //cameraSpeed.x = velocityX;
        //cameraSpeed.y = velocityY;
    }

    public void Zoom(float originalDistance,float currentDistance)
    {
        camera.zoom = originalDistance/currentDistance;
    }

    private void loadImages(){
        asteroids = new Texture[4];
        asteroids[0] = new Texture(Gdx.files.internal("Asteroids/A1_red.png"));
    }

    private void initSkin(){
        skin = new Skin();

        //SKIN RESOURCES
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white", new Texture(pixmap));
        //dispose pixmap ?

        /*Pixmap p2 = new Pixmap(5, 50, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.GREEN);
        pixmap.fill();
        skin.add("cursor", new Texture(pixmap));*/

        skin.add("default",game.font);

        //TEXT BUTTON SKIN
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = skin.newDrawable("white", Color.DARK_GRAY);
        textButtonStyle.down = skin.newDrawable("white", Color.DARK_GRAY);
        textButtonStyle.checked = skin.newDrawable("white", Color.BLUE);
        textButtonStyle.over = skin.newDrawable("white", Color.LIGHT_GRAY);
        textButtonStyle.font = skin.getFont("default");
        skin.add("default", textButtonStyle);

        //LABEL SKIN
        Label.LabelStyle labelstyle = new Label.LabelStyle();
        labelstyle.font = skin.getFont("default");
        skin.add("default", labelstyle);

        //TEXTFIELD SKIN
        TextField.TextFieldStyle textfieldstyle = new TextField.TextFieldStyle();
        textfieldstyle.font = skin.getFont("default");
        textfieldstyle.fontColor = Color.BLACK;
        textfieldstyle.cursor = skin.newDrawable("white", Color.OLIVE);
        textfieldstyle.cursor.setMinWidth(5.f);
        skin.add("default",textfieldstyle);

        //SCROLLPANE SKIN
        ScrollPane.ScrollPaneStyle scrollpanestyle = new ScrollPane.ScrollPaneStyle();
        skin.add("default",scrollpanestyle);

        //SLIDER SKIN
        Slider.SliderStyle sliderstyle = new Slider.SliderStyle();
        sliderstyle.background = skin.newDrawable("white",Color.OLIVE);
        skin.add("default-horizontal",sliderstyle);
    }
}
