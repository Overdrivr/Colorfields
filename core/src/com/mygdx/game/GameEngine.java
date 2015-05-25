package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
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
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

/**
 * Created by Bart on 03/05/2015.
 */
public class GameEngine {

    //PHYSICS
    public World world;
    private float accumulator = 0;
    private Vector<Body> spheres;
    //Vector2 sunPosition;
    Vector2 cannonPosition;

    b2Separator splitter;
    ContourToPolygons triangulator;

    GravityField field;

    //Image processing
    PNGtoBox2D converter;

    AssetManager assetManager;

    //Game objects
    Stage stage;
    public Array<MassiveAsteroid> massiveAsteroids;
    public Array<SphereOre> ores;

    //debug objects
    ShapeRenderer renderer;

    // Level properties
    float worldSize;
    float camerabound_plus_x;
    float camerabound_minus_x;
    float camerabound_plus_y;
    float camerabound_minus_y;

    public GameEngine(final Stage s){
        stage = s;

        //Init tools
        converter = new PNGtoBox2D();
        splitter = new b2Separator();
        triangulator = new ContourToPolygons();
        assetManager = new AssetManager();
        renderer = new ShapeRenderer();

        initLevel();
    }

    private void initLevel(){
        // Level properties
        worldSize = 5000.f;
        camerabound_plus_x = worldSize/2.f;
        camerabound_minus_x = -worldSize/2.f;
        camerabound_plus_y = worldSize/2.f;
        camerabound_minus_y = -worldSize/2.f;
        cannonPosition = new Vector2(-400.f,10.f);

        // Init physics world
        world = new World(new Vector2(0.f,0.f),true);

        spheres = new Vector();

        createStartPoint(new Vector2(-400, 1));


        // Grid
        field = new GravityField(new Vector2(-500,-500),50,20.f);
        field.addSphericalAttractor(new Vector2(-200,-200));
        field.debugDrawGrid = false;

        // Massive asteroids
        massiveAsteroids = new Array<MassiveAsteroid>();
        massiveAsteroids.add(new MassiveAsteroid(this,"Asteroids/A1_red.png",new Vector2(-250.f,0.f),3.f));
        // massiveAsteroids.add(new MassiveAsteroid(this,"TestAssets/test_asset_1_contours.png",new Vector2(-750.f,100.f),3.f));
        // massiveAsteroids.add(new MassiveAsteroid(this,"TestAssets/test_asset_2_contours.png",new Vector2(-500.f,350.f),3.f));
        //massiveAsteroids.add(new MassiveAsteroid(this,"TestAssets/test_asset_3.png",new Vector2(0.f,700.f),1.f));
        //massiveAsteroids.add(new MassiveAsteroid(this,"TestAssets/test_asset_4.png",new Vector2(0.f,400.f),1.f));
        massiveAsteroids.add(new MassiveAsteroid(this,"Asteroids/A2_orange.png",new Vector2(-850.f,150.f),3.f));

        // Small asteroids
        ores = new Array<SphereOre>();
        ores.add(new SphereOre(this,"TestAssets/doublesquare.png",new Vector2(-100.f,-60.f)));
        ores.add(new SphereOre(this, "TestAssets/doublesquare.png", new Vector2(-200.f, -30.f)));
        ores.add(new SphereOre(this, "TestAssets/doublesquare.png", new Vector2(-300.f, 60.f)));
    }

    public void doPhysicsStep(float deltaTime) {
        // fixed time step
        // max frame time to avoid spiral of death (on slow devices)
        float frameTime = Math.min(deltaTime, 0.25f);
        accumulator += frameTime;
        while (accumulator >= 1/45.f) {
            world.step(1/45.f, 6, 2);
            world.clearForces();
            updateFields();
            accumulator -= 1/45.f;
        }
    }

    private void updateFields(){
        //Update gravity forces
        for(Body b : spheres){
            Vector2 force = field.getForce(b.getPosition().x,b.getPosition().y);
            b.applyForce(force,b.getPosition(),true);
        }
    }

    private void createStartPoint(Vector2 position){
        // Init the starting point of thrown objects (ore)

        BodyDef groundBodyDef = new BodyDef();
        groundBodyDef.position.set(position);

        Body groundBody = world.createBody(groundBodyDef);

        PolygonShape groundBox = new PolygonShape();
        groundBox.setAsBox(10.f, 1.0f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = groundBox;
        fixtureDef.isSensor = true;
        groundBody.createFixture(fixtureDef);

        groundBox.dispose();
    }

    public void createSphere(float x, float y)
    {
        // Create a new ore to throw from starting point

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(cannonPosition);

        Body body = world.createBody(bodyDef);

        CircleShape circle = new CircleShape();
        circle.setRadius(2.5f);

        spheres.add(body);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 5.0f;
        fixtureDef.restitution = 0.6f;

        Fixture fixture = body.createFixture(fixtureDef);

        // Compute shooting vector
        Vector2 shootingVector = new Vector2();
        shootingVector.x = x - cannonPosition.x;
        shootingVector.y = y - cannonPosition.y;
        shootingVector.setLength(1000.f);

        body.applyLinearImpulse(shootingVector, cannonPosition, true);

        circle.dispose();
    }

    private void testarea(){
        //Define a concave shape
        Array<Vector2> v = new Array<Vector2>();
        v.add(new Vector2(200,200));
        v.add(new Vector2(180,170));
        v.add(new Vector2(190,130));
        v.add(new Vector2(220,120));
        v.add(new Vector2(220, 90));
        v.add(new Vector2(210,70));
        v.add(new Vector2(230,40));
        v.add(new Vector2(270,90));
        v.add(new Vector2(250, 140));
        v.add(new Vector2(260,160));

        //Create body
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(new Vector2(-100.f,0.f));

        // Create a fixture definition to apply our shape to
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.8f;
        fixtureDef.restitution = 0.6f; // Make it bounce a little bit

        // Create our body in the world using our body definition
        Body body = world.createBody(bodyDef);

        b2Separator splitter = new b2Separator();
        ContourToPolygons triangulator = new ContourToPolygons();

        Vector2[] z = v.toArray(Vector2.class);
        //splitter.separate(body,fixtureDef,z);
        triangulator.BuildShape(body, fixtureDef, v);
    }

    public void debugDraw(Matrix4 combined){
        renderer.setProjectionMatrix(combined);
        renderer.begin(ShapeRenderer.ShapeType.Line);

        field.debug_draw(renderer,combined);
        //Draw XY reference
        float x1 = 0;
        float x2 = 10;
        float y1 = 0;
        float y2 = 0;
        renderer.setColor(1,0,0,1);
        renderer.line(x1, y1, x2, y2);
        x1 = 0;
        x2 = 0;
        y1 = 0;
        y2 = 10;
        renderer.setColor(0,1,0,1);
        renderer.line(x1, y1, x2, y2);

        renderer.end();
    }
}
