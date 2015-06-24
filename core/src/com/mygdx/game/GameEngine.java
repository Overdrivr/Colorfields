package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.JointDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.physics.box2d.joints.RopeJointDef;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Vector;

/**
 * Created by Bart on 03/05/2015.
 */

public class GameEngine {

    //PHYSICS
    public World world;
    private float accumulator = 0;
    private Vector<Convoy> convoys;
    Vector2 cannonPosition;
    private EndPoint endPoint;

    b2Separator splitter;
    ContourToPolygons triangulator;

    GravityField field;
    MyContactListener contactListener;

    //Image processing
    PNGtoBox2D converter;

    AssetManager assetManager;

    //Game objects
    Stage stage;
    public Array<MassiveAsteroid> massiveAsteroids;
    public Array<SphereOre> ores;
    boolean shootingInPreparation;

    TimeUtils chrono;
    long startingTime;

    Random rnd;

    //debug objects
    ShapeRenderer renderer;

    // Level properties
    float worldSize;
    float camerabound_plus_x;
    float camerabound_minus_x;
    float camerabound_plus_y;
    float camerabound_minus_y;

    public LinkedList<JointDef> jointsToBuild;
    public LinkedList<Joint> joints;
    public LinkedList<Joint> jointsToDestroy;
    public LinkedList<Body> bodiesToDestroy;
    public LinkedList<Convoy> convoysToDestroy;


    // player properties
    int score = 0;

    // Constants
    long maxChargeDuration = 1000;
////////////////////////////////////////////////////////////////////////////
    public GameEngine(final Stage s){
        stage = s;

        //Init tools
        converter = new PNGtoBox2D();
        splitter = new b2Separator();
        triangulator = new ContourToPolygons();
        assetManager = new AssetManager();
        renderer = new ShapeRenderer();
        rnd = new Random();
        initLevel();
    }

    private void initLevel(){
        // Level properties
        worldSize = 50.f;
        camerabound_plus_x = worldSize/2.f;
        camerabound_minus_x = -worldSize/2.f;
        camerabound_plus_y = worldSize/2.f;
        camerabound_minus_y = -worldSize/2.f;
        cannonPosition = new Vector2(-4.f,0.1f);

        // Init physics world
        world = new World(new Vector2(0.f,0.f),true);
        contactListener = new MyContactListener(this);
        world.setContactListener(contactListener);

        jointsToBuild = new LinkedList<JointDef>();
        bodiesToDestroy = new LinkedList<Body>();
        joints = new LinkedList<Joint>();
        jointsToDestroy = new LinkedList<Joint>();
        convoysToDestroy = new LinkedList<Convoy>();

        convoys = new Vector();

        createStartPoint(new Vector2(-4, 0.01f));
        endPoint = new EndPoint(this,new Vector2(3, 4),2.5f);

        // Grid
        field = new GravityField(new Vector2(-worldSize/2,-worldSize/2),worldSize,100);
        field.addSphericalAttractor(new Vector2(-2, -2),new Color(1,0,0,1));
        field.debugDrawGrid = false;

        field.addSphericalAttractor(new Vector2(3,4),new Color(1,0,0,1));
        field.addSphericalAttractor(new Vector2(3,-2),new Color(0,1,0,1));

        // Massive asteroids
        massiveAsteroids = new Array<MassiveAsteroid>();
        massiveAsteroids.add(new MassiveAsteroid(this,"Asteroids/A1_red.png",new Vector2(0,0),0.03f));
        massiveAsteroids.add(new MassiveAsteroid(this,"Asteroids/A2_orange.png",new Vector2(-20,1.5f),0.03f));

        // Small asteroids
        ores = new Array<SphereOre>();
        ores.add(new SphereOre(this,"TestAssets/doublesquare.png",
                new Vector2(-1.f,-0.6f)));
        ores.add(new SphereOre(this, "TestAssets/doublesquare.png",
                new Vector2(-2.f, -0.3f)));
        ores.add(new SphereOre(this, "TestAssets/doublesquare.png",
                new Vector2(-3.f, 0.6f)));

        shootingInPreparation = false;

        chrono = new TimeUtils();
    }

    public void doPhysicsStep(float deltaTime) {
        // fixed time step
        // max frame time to avoid spiral of death (on slow devices)
        float frameTime = Math.min(deltaTime, 0.25f);
        accumulator += frameTime;
        while (accumulator >= 1/45.f) {
            world.step(1 / 45.f, 6, 2);
            world.clearForces();
            accumulator -= 1/45.f;
        }
        // Update forces from gravity field
        updateFields();

        while(!convoysToDestroy.isEmpty()){
            Convoy c = convoysToDestroy.pop();
            c.DestroyContainer();
        }

        // Empty the joint list to build
        // This list is filled during the step function, where it is forbidden to create joints
        while(!jointsToBuild.isEmpty()){
            // Get the joint definition
            JointDef jointDef = jointsToBuild.pop();
            // Build it
            MouseJoint joint = (MouseJoint) world.createJoint(jointDef);
            // Move target point to final point so that the body is attracted to final point
            joint.setTarget(endPoint.m_position);
        }
    }

    private void updateFields(){
        //Update gravity forces
        for(Convoy c : convoys){
            c.update(field);
        }
    }

    private void createStartPoint(Vector2 position){
        // Init the starting point of thrown objects (ore)

        BodyDef groundBodyDef = new BodyDef();
        groundBodyDef.position.set(position);

        Body groundBody = world.createBody(groundBodyDef);

        PolygonShape groundBox = new PolygonShape();
        groundBox.setAsBox(0.3f, 0.02f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = groundBox;
        fixtureDef.isSensor = true;
        groundBody.createFixture(fixtureDef);

        groundBox.dispose();
    }

    private void createConvoy(float x, float y, float force) {
        // Create a new convoy to throw from cannonPosition to (x,y)
        // Amount of spheres (+1 for the initial sphere)
        int amount = rnd.nextInt(10);

        // Compute direction vector of the convoy
        Vector2 directionVector = new Vector2();
        directionVector.x = x - cannonPosition.x;
        directionVector.y = y - cannonPosition.y;

        // Compute force vector
        Vector2 forceVector = new Vector2(directionVector);
        forceVector.setLength(force);

        // Create new convoy
        convoys.add(new Convoy(this, cannonPosition, directionVector, forceVector, amount));
    }

    public void startCharge(){
        shootingInPreparation = true;
        startingTime = chrono.millis();
    }

    public void endCharge(float x, float y){

        if(shootingInPreparation){
            long elapsedTime = chrono.timeSinceMillis(startingTime);

            if(elapsedTime > maxChargeDuration)
                elapsedTime = maxChargeDuration;

            // Formula for initial force
            // f = e^2 * constant
            float force = elapsedTime / 100;
            force *= elapsedTime;
            force *= 0.000005f;

            createConvoy(x, y, force);
            shootingInPreparation = false;
        }

    }

    public void cancelCharge(){
        shootingInPreparation = false;
    }

    public float getChargePercent(){
        if(shootingInPreparation){
            long elapsedTime = chrono.timeSinceMillis(startingTime);
            float percent = elapsedTime / (float)(maxChargeDuration) * 100.f;
            if(percent > 100.f)
                percent = 100.f;
            return percent;
        }
        else
            return 0;
    }

    public void oreReachedEndpoint(Body b){
        //TODO : try except
        //Get convoy
        MyBodyData data = (MyBodyData)(b.getUserData());
        //Start tracking system
        endPoint.startCapture(data.convoy);
    }

    public void oreReachedEndpointFinal(Body b){
        score++;
        //Tell endpoint that a convoy has reached final destination
        endPoint.endCapture(b);
    }

    public int getScore(){
        return score;
    }

    /* TODO : This method involves render only and has nothing to do here*/
    public void debugDraw(Matrix4 combined){
        renderer.setProjectionMatrix(combined);
        renderer.begin(ShapeRenderer.ShapeType.Line);

        field.debug_draw(renderer,combined);
        //Draw XY reference
        float x1 = -worldSize/2;
        float x2 = +worldSize/2;
        float y1 = 0;
        float y2 = 0;
        renderer.setColor(1,0,0,1);
        renderer.line(x1, y1, x2, y2);
        x1 = 0;
        x2 = 0;
        y1 = -worldSize/2;
        y2 = +worldSize/2;
        renderer.setColor(0,1,0,1);
        renderer.line(x1, y1, x2, y2);

        renderer.end();
    }
}
