package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
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

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.FillViewport;

import java.util.Iterator;
import java.util.Locale;
import java.util.Random;
import java.util.Vector;

import sun.swing.CachedPainter;

/**
 * Created by Bart on 26/04/2015.
 */
public class GameScreen implements Screen {

    final MyGdxGame game;

    FillViewport viewport;
    Vector2 cameraSpeed;
    float initial_zoom;

    //EFFECTS
    private ParticleEffect effect;

    //Internationalization
    public I18NBundle myBundle;

    Stage stage;
    Stage hudStage;
    Table table;
    Skin skin;
    ProgressBar bar;
    Label score;

    // For debug drawing
    private ShapeRenderer shapeRenderer;
    private Box2DDebugRenderer debugRenderer;

    public GameEngine engine;

    // Constants
    float zoomMin = 0.001f;
    float zoomMax = 0.05f;
    float zoomDefault = 0.005f;

    public GameScreen(final MyGdxGame g) {

        game = g;
        initSkin();

        // Stage controls the rendering process
        // Viewports helps managing the camera render aera in function of the device
        viewport = new FillViewport(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        stage = new Stage(viewport,g.batch);
        stage.getCamera().translate(-Gdx.graphics.getWidth()/2.f,-Gdx.graphics.getHeight()/2.f,0.f);
        ((OrthographicCamera)this.stage.getCamera()).zoom = zoomDefault;
        cameraSpeed = new Vector2(0,0);

        //Init game engine
        engine = new GameEngine(stage);


        //Internalization
        FileHandle baseFileHandle = Gdx.files.internal("I18N/GameScreenBundle");
        Locale locale = new Locale("en", "GB");
        myBundle = I18NBundle.createBundle(baseFileHandle, locale);

        // UI
        hudStage = new Stage(new FillViewport(Gdx.graphics.getWidth(),Gdx.graphics.getHeight()),g.batch);
        //Compute padding
        //TODO : to use
        float padding = Gdx.graphics.getHeight()/20;
        Gdx.app.log("PADDING",Float.toString(padding));
        table = new Table(skin);
        table.setFillParent(true);
        //table.debug();
        hudStage.addActor(table);

        table.top();
        Label scorelabel = new Label(myBundle.get("score"),skin);
        table.add(scorelabel).expandX();

        bar = new ProgressBar(0,100,1,false,skin);
        bar.setValue(0);
        bar.setSize(bar.getPrefWidth() * 5, bar.getPrefHeight());
        table.add(bar);

        score = new Label("0",skin);
        table.add(score).expandX();

        // Event management
        MyGestureListener gestureListener = new MyGestureListener();
        gestureListener.setGamescreen(this);

        MyGestureDetector detector = new MyGestureDetector(gestureListener,this);
        detector.setLongPressSeconds(0.4f);

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(hudStage);
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(detector);
        Gdx.input.setInputProcessor(multiplexer);

        //PARTICLE EFFECTS
        effect = new ParticleEffect();
        effect.load(Gdx.files.internal("Particles/green_peaceful_flame"), Gdx.files.internal("Particles"));
        effect.setPosition(300, 300);
        effect.findEmitter("Fire").setContinuous(true);
        effect.start();

        shapeRenderer = new ShapeRenderer();

        //Debug rendering
        debugRenderer = new Box2DDebugRenderer();
        debugRenderer.setDrawVelocities(true);
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Let camera slide at the end of the fling
        stage.getCamera().translate(cameraSpeed.x * delta, cameraSpeed.y * delta, 0.f);
        final float alpha = 0.81f;
        // Increase alpha when zoom has a low value (big zoom)
        cameraSpeed.x *= alpha;
        cameraSpeed.y *= alpha;

        stage.act(delta);
        stage.getCamera().update();
        engine.debugDraw(stage.getCamera().combined);
        stage.draw();

        debugRenderer.render(engine.world, stage.getViewport().getCamera().combined);

        hudStage.act(delta);
        hudStage.draw();

        engine.doPhysicsStep(delta);


        float force = engine.getChargePercent();
        bar.setValue(force);

        score.setText(Integer.toString(engine.getScore()));
    }

    public Vector2 screenVectorToWorld(Vector2 velocity){
        Vector2 start = stage.getViewport().unproject(new Vector2(0,0));
        Vector2 end = stage.getViewport().unproject(velocity);

        // Compute speed in world coordinates to account for zoom levels
        Vector2 diff = start.mulAdd(end, -1);

        return diff;
    }


    @Override
    public void resize(int x, int y){
        stage.getViewport().update(x, y, false);
        hudStage.getViewport().update(x,y,false);
    }

    @Override
    public void show(){

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
        hudStage.dispose();
        shapeRenderer.dispose();
    }

    public void touchUpAction(float x, float y){
        Vector2 v = stage.getViewport().unproject(new Vector2(x,y));
        engine.endCharge(v.x, v.y);
    }

    public void longPressAction(float x, float y){
        Vector2 w = stage.getViewport().unproject(new Vector2(x, y));
        engine.startCharge();
    }

    //EVENTS
    //Used to store initial zoom
    public void TouchDown(){
        initial_zoom = ((OrthographicCamera)this.stage.getCamera()).zoom;
        cameraSpeed.x = 0;
        cameraSpeed.y = 0;
        engine.cancelCharge();
    }


    public void Tap(float x, float y){

    }

    public void Pan(Vector2 start, Vector2 end)
    {
        // Translate start and end position from screen to world
        Vector2 start_world = stage.getViewport().unproject(start);
        Vector2 end_world = stage.getViewport().unproject(end);
        // Compute delta in world coordinates
        // So that, no matter the zoom the physical move of the finger
        // will correspond to the same translation in the world
        Vector2 diff = start_world.mulAdd(end_world,-1);

        stage.getCamera().translate(diff.x,diff.y,0.f);
    }

    public void Fling(float velocityX, float velocityY){
        Vector2 start = stage.getViewport().unproject(new Vector2(0,0));
        Vector2 end = stage.getViewport().unproject(new Vector2(velocityX,velocityY));

        // Compute speed in world coordinates to account for zoom levels
        Vector2 diff = start.mulAdd(end, -1);

        cameraSpeed.x = diff.x;
        cameraSpeed.y = diff.y;
    }

    public void Zoom(float originalDistance,float currentDistance)
    {
        float ratio = originalDistance / currentDistance;
        float final_zoom = MathUtils.clamp(initial_zoom * ratio, zoomMin, zoomMax);
        ((OrthographicCamera)this.stage.getCamera()).zoom = final_zoom;
    }

    private void initSkin(){
        skin = new Skin();

        //SKIN RESOURCES
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white", new Texture(pixmap));

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

        //TOUCHPAD SKIN
        Touchpad.TouchpadStyle touchpadStyle = new Touchpad.TouchpadStyle();
        skin.add("touchpad_background",new Texture(Gdx.files.internal("UI/touchpad_background.png")));
        skin.add("touchpad_knob",new Texture(Gdx.files.internal("UI/touchpad_knob.png")));
        touchpadStyle.background = skin.getDrawable("touchpad_background");
        touchpadStyle.knob = skin.getDrawable("touchpad_knob");
        skin.add("default",touchpadStyle);

        //IMAGE BUTTON SKIN
        ImageButton.ImageButtonStyle imageButtonStyle = new ImageButton.ImageButtonStyle();
        imageButtonStyle.imageUp = skin.getDrawable("touchpad_background");
        skin.add("default",imageButtonStyle);

        //PROGRESS BAR SKIN
        ProgressBar.ProgressBarStyle barStyle = new ProgressBar.ProgressBarStyle();
        skin.add("progressbar_background",new Texture(Gdx.files.internal("UI/loadbar_background.png")));
        skin.add("progressbar_knob",new Texture(Gdx.files.internal("UI/loadbar_knob.png")));
        barStyle.background = skin.getDrawable("progressbar_background");
        barStyle.knob = skin.getDrawable("progressbar_knob");
        skin.add("default-horizontal",barStyle);
    }
}
