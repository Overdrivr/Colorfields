package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
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
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.overdrivr.model.GameEngine;

import java.util.Locale;

/**
 * Created by Bart on 26/04/2015.
 */
public class GameScreen extends ScreenAdapter {

    public final MyGdxGame topApplication;
    public final GameEngine engine;
    public I18NBundle myBundle;
    FillViewport viewport;
    OrthographicCamera camera;

    Stage hudStage;
    Table table;
    Skin skin;
    ProgressBar bar;
    Label score;

    // For debug drawing
    private ShapeRenderer shapeRenderer;
    private Box2DDebugRenderer debugRenderer;


    float initial_zoom;

    ///////////////////////////////////// Global constants
    // TODO : Retrieve these values from Model
    float zoomMin = 0.001f;
    float zoomMax = 0.05f;
    float zoomDefault = 0.005f;
    float camerabound_plus_x = 100.f;
    float camerabound_minus_x = -100.f;
    float camerabound_plus_y = 100.f;
    float camerabound_minus_y = -100.f;
    final float alpha = 0.81f;
    float worldSize;
    Vector2 cameraSpeed;


    public GameScreen(final MyGdxGame g) {

        topApplication = g;
        engine = topApplication.engine;
        worldSize = engine.worldSize;

        FileHandle baseFileHandle = Gdx.files.internal("I18N/GameScreenBundle");
        myBundle = I18NBundle.createBundle(baseFileHandle, topApplication.locale);

        initSkin();

        cameraSpeed = new Vector2(0,0);

        camera = new OrthographicCamera();
        camera.zoom = zoomDefault;
        camera.near = 100.f;
        camera.far = -100.f;
        camera.update();
        camera.position.set(0,0,0);

        // Viewports helps managing the camera render area in function of the device
        viewport = new FillViewport(Gdx.graphics.getWidth(),Gdx.graphics.getHeight(),camera);

        // UI
        hudStage = new Stage(new FillViewport(Gdx.graphics.getWidth(),Gdx.graphics.getHeight()),topApplication.batch2D);
        //Compute padding
        //TODO : to use
        float padding = Gdx.graphics.getHeight()/20;
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

        // High level interpretation of gesture
        MyGestureListener gestureListener = new MyGestureListener(this);
        // Just to know when touch down has been pressed
        MyGestureDetector detector = new MyGestureDetector(gestureListener,this);
        detector.setLongPressSeconds(0.4f);

        // Multiplexing the events to the app's different UI layers
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(hudStage);
        //multiplexer.addProcessor(stage);
        multiplexer.addProcessor(detector);
        Gdx.input.setInputProcessor(multiplexer);

        shapeRenderer = new ShapeRenderer();

        //Debug rendering
        debugRenderer = new Box2DDebugRenderer();
        debugRenderer.setDrawVelocities(true);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.9f, 0.9f, 0.9f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // Let camera slide at the end of the fling
        camera.translate(cameraSpeed.x * delta, cameraSpeed.y * delta, 0.f);
        // Increase alpha when zoom has a low value (big zoom)
        cameraSpeed.x *= alpha;
        cameraSpeed.y *= alpha;
        camera.update();

        debugDraw(camera.combined);

        updateUI();

        engine.update(delta);
        hudStage.act(delta);

        engine.render(topApplication.batch3D, topApplication.batch2D,camera);

        debugRenderer.render(engine.world, camera.combined);
        hudStage.draw();
    }

    public void updateUI(){
        // Update charging bar
        float force = engine.getChargePercent();
        bar.setValue(force);
        score.setText(Integer.toString(engine.getScore()));
    }

    @Override
    public void resize(int x, int y){
        viewport.update(x, y, false);
        hudStage.getViewport().update(x,y,false);
    }

    @Override
    public void dispose(){
        //stage.dispose();
        hudStage.dispose();
        shapeRenderer.dispose();
    }

    public void touchUpAction(float x, float y){
        Vector2 v = viewport.unproject(new Vector2(x,y));
        engine.endCharge(v.x, v.y);
    }

    public void longPressAction(float x, float y){
        Vector2 w = viewport.unproject(new Vector2(x, y));
        engine.startCharge();
    }

    //EVENTS
    //Used to store initial zoom
    public void TouchDown(){
        initial_zoom = camera.zoom;
        cameraSpeed.x = 0;
        cameraSpeed.y = 0;
        engine.cancelCharge();
    }


    public void Tap(float x, float y){

    }

    public void Pan(Vector2 start, Vector2 end)
    {
        // Translate start and end position from screen to world
        Vector2 start_world = viewport.unproject(start);
        Vector2 end_world = viewport.unproject(end);
        // Compute delta in world coordinates
        // So that, no matter the zoom the physical move of the finger
        // will correspond to the same translation in the world
        Vector2 diff = start_world.mulAdd(end_world,-1);

        camera.translate(diff.x,diff.y,0.f);

        //Gdx.app.log("GameScreen", "Start(" + start_world.x + " ; " + start_world.y + ")");
        //Gdx.app.log("GameScreen", "Stop(" + end_world.x + " ; " + end_world.y + ")");
        //Gdx.app.log("GameScreen",camera.position.x+" ; "+camera.position.y);

        // Prevent camera from going outside world
        // TODO : Use frustum to compute edge location at game plane level
        // If the camera is outside, bring it back
    }

    public void Fling(float velocityX, float velocityY){
        Vector2 start = viewport.unproject(new Vector2(0,0));
        Vector2 end = viewport.unproject(new Vector2(velocityX,velocityY));

        // Compute speed in world coordinates to account for zoom levels
        Vector2 diff = start.mulAdd(end, -1);

        cameraSpeed.x = diff.x;
        cameraSpeed.y = diff.y;
    }

    public void Zoom(float originalDistance,float currentDistance)
    {
        float ratio = originalDistance / currentDistance;
        float final_zoom = MathUtils.clamp(initial_zoom * ratio, zoomMin, zoomMax);
        camera.zoom = final_zoom;
    }

    public void debugDraw(Matrix4 combined){
        shapeRenderer.setProjectionMatrix(combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        engine.field.debug_draw(shapeRenderer,combined);
        //Draw XY reference
        float x1 = -worldSize/2;
        float x2 = +worldSize/2;
        float y1 = 0;
        float y2 = 0;
        shapeRenderer.setColor(1,0,0,1);
        shapeRenderer.line(x1, y1, x2, y2);
        x1 = 0;
        x2 = 0;
        y1 = -worldSize/2;
        y2 = +worldSize/2;
        shapeRenderer.setColor(0,1,0,1);
        shapeRenderer.line(x1, y1, x2, y2);

        shapeRenderer.end();
    }

    //Todo : to replace with external file
    private void initSkin(){
        skin = new Skin();

        //SKIN RESOURCES
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white", new Texture(pixmap));

        skin.add("default",topApplication.font);

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
