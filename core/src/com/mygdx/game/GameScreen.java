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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.FillViewport;


import java.util.Iterator;
import java.util.Locale;
import java.util.Random;
import java.util.Vector;

/**
 * Created by Bart on 26/04/2015.
 */
public class GameScreen implements Screen {

    final MyGdxGame game;
    //OrthographicCamera camera;
    final float camerabase = 800;
    float cameraunitsx;
    float cameraunitsy;
    Vector2 cameraSpeed;
    float initial_zoom;

    //EFFECTS
    private ParticleEffect effect;

    //Game textures
    private Texture[] asteroids;
    private Texture contour, contour2;

    Random rnd;

    //Internationalization
    public I18NBundle myBundle;

    Stage stage;
    Skin skin;

    // For debug drawing
    private ShapeRenderer shapeRenderer;
    private Box2DDebugRenderer debugRenderer;

    public GameEngine engine;

    public GameScreen(final MyGdxGame g) {

        //To test specific portions of code
        //testaera();

        game = g;
        initSkin();

        // Stage controls the rendering process
        // Viewports helps managing the camera render aera in function of the device
        stage = new Stage(new FillViewport(Gdx.graphics.getWidth(),Gdx.graphics.getHeight()));
        cameraSpeed = new Vector2(0,0);

        //Init game engine
        engine = new GameEngine(stage);


        //Internalization
        FileHandle baseFileHandle = Gdx.files.internal("I18N/GameScreenBundle");
        Locale locale = new Locale("en", "GB");
        myBundle = I18NBundle.createBundle(baseFileHandle, locale);

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
        effect.load(Gdx.files.internal("Particles/green_peaceful_flame"), Gdx.files.internal("Particles"));
        effect.setPosition(300, 300);
        effect.findEmitter("Fire").setContinuous(true);
        effect.start();

        //UI Init
        shapeRenderer = new ShapeRenderer();
        //Gdx.input.setInputProcessor(stage);

        //Debug rendering
        debugRenderer = new Box2DDebugRenderer();
        debugRenderer.setDrawVelocities(true);
        debugRenderer.setDrawAABBs(true);


        asteroids[0] = new Texture(Gdx.files.internal("TestAssets/grid.png"));
        Image testasset = new Image(asteroids[0]);
        stage.addActor(testasset);
    }



    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Let camera slide at the end of the fling
        if(cameraSpeed.len() > 0.001f)
        {

        }
        stage.getCamera().translate(cameraSpeed.x, cameraSpeed.y, 0.f);
        final float alpha = 0.81f;
        // Increase alpha when zoom has a low value (big zoom)
        cameraSpeed.x *= alpha;
        cameraSpeed.y *= alpha;

        stage.act(delta);
        stage.draw();

        debugRenderer.render(engine.world, stage.getViewport().getCamera().combined);
        engine.doPhysicsStep(delta);

        /*game.batch.setProjectionMatrix(camera.combined);
        // Render
        game.batch.begin();
        game.batch.draw(asteroids[0],0,0);
        game.batch.end();*/
        /*

        if(cameraSpeed.len() < 0.001f)
        {
            cameraSpeed.x = 0;
            cameraSpeed.y = 0;
        }

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        // Update particles
        //effect.update(delta);

        // Render
        game.batch.begin();

        for(int i = 0 ; i < engine.massiveAsteroids.size ; i++){
           engine.massiveAsteroids.get(i).draw(game.batch);
        }
        game.batch.draw(asteroids[0],0,0);
        //effect.draw(game.batch);
        debugRenderer.render(engine.world, camera.combined);
        game.batch.end();
        engine.doPhysicsStep(delta);*/
/*
        stage.act(delta);
        stage.draw();
        */
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

    //EVENTS
    //Used to store initial zoom
    public void TouchDown(){
        initial_zoom = ((OrthographicCamera)this.stage.getCamera()).zoom;
        Gdx.app.log("TAP","Initial ("+Float.toString(initial_zoom)+")");
    }
    public void Tap(float x, float y){
        Vector2 w = stage.getViewport().unproject(new Vector2(x,y));
        engine.createSphere(w.x, w.y);
        //Gdx.app.log("TAP","Screen ("+Float.toString(x)+","+Float.toString(y)+")");
        //Gdx.app.log("TAP", "World (" + Float.toString(w.x) + "," + Float.toString(w.y) + ")");
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

        //Gdx.app.log("PAN", "World (" + Float.toString(diff.x)+","+Float.toString(diff.y) + ")");
    }

    public void Fling(float velocityX, float velocityY){
        cameraSpeed.x = -velocityX/100;
        cameraSpeed.y =  velocityY/100;

        //Gdx.app.log("FLING", "Elasticity (" + Float.toString(cameraSpeed.x) + "," +
        //                                      Float.toString(cameraSpeed.y) + ")");

        //if(cameraSpeed.len() > 200)
         //   cameraSpeed.setLength(200);
    }

    public void Zoom(float originalDistance,float currentDistance)
    {
        float ratio = originalDistance / currentDistance;
        float final_zoom = MathUtils.clamp(initial_zoom * ratio, 0.3f, 10.0f);
        ((OrthographicCamera)this.stage.getCamera()).zoom = final_zoom;

        //Gdx.app.log("Zoom", "Final Distance (" + Float.toString(final_zoom) + ")");
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

    private void testaera(){
        Array<Vector2> l1 = new Array<Vector2>();

        l1.add(new Vector2(0,3));
        l1.add(new Vector2(1,6));
        l1.add(new Vector2(2,25));
        l1.add(new Vector2(3,19));
        l1.add(new Vector2(4,16));
        l1.add(new Vector2(5,12));
        l1.add(new Vector2(6,4));
        l1.add(new Vector2(7,6));

        PNGtoBox2D converter = new PNGtoBox2D();
        l1 = converter.RDP(l1,1.f);

        Gdx.app.log("DEBUG","-----");
        for(int i = 0 ; i < l1.size ; i++){
            Gdx.app.log(Float.toString(l1.get(i).x),Float.toString(l1.get(i).y));
        }
    }
}
