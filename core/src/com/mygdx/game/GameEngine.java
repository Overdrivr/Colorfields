package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
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
    Vector2 sunPosition;
    Vector2 cannonPosition;

    public GameEngine(){
        initPhysics();
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
            //Compute vector from center of body to center of sun
            Vector2 v = b.getPosition();
            float r = v.dst(sunPosition);
            v.x = sunPosition.x - v.x;
            v.y = sunPosition.y - v.y;
            //Normalize and apply gravity force toward the sun
            float force = 1000000.f / (r * r);

            v.setLength(force);
            //Gdx.app.log("Force",Float.toString(v.x)+" "+Float.toString(v.y));
            //Apply force to each body
            b.applyForce(v, b.getWorldCenter(), true);
        }
    }

    private void initPhysics(){
        //PHYSICS
        world = new World(new Vector2(0.f,0.f),true);
        sunPosition = new Vector2(0.f,90.f);
        cannonPosition = new Vector2(1.f,10.f);

        spheres = new Vector();

        // Create our body definition
        BodyDef groundBodyDef = new BodyDef();
        // Set its world position
        groundBodyDef.position.set(new Vector2(0, 1));

        // Create a body from the definition and add it to the world
        Body groundBody = world.createBody(groundBodyDef);

        // Create a polygon shape
        PolygonShape groundBox = new PolygonShape();
        // Set the polygon shape as a box which is twice the size of our view port and 20 high
        // (setAsBox takes half-width and half-height as arguments)
        groundBox.setAsBox(10.f, 1.0f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = groundBox;
        fixtureDef.isSensor = true;

        // Create a fixture from our polygon shape and add it to our ground body
        groundBody.createFixture(fixtureDef);
        // Clean up after ourselves
        groundBox.dispose();

        //Create a sun
        BodyDef sunBodyDef = new BodyDef();
        sunBodyDef.position.set(sunPosition);
        Body sunBody = world.createBody(sunBodyDef);
        CircleShape sunShape = new CircleShape();
        sunShape.setRadius(5.0f);
        sunBody.createFixture(sunShape, 0.0f);
        sunShape.dispose();

        //Create an asteroid
        createAsteroid();

        //test aera
        testarea();
    }

    public void createSphere(float x, float y)
    {
        // First we create a body definition
        BodyDef bodyDef = new BodyDef();
        // We set our body to dynamic, for something like ground which doesn't move we would set it to StaticBody
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        // Set our body's starting position in the world
        bodyDef.position.set(cannonPosition);

        // Create our body in the world using our body definition
        Body body = world.createBody(bodyDef);

        // Create a circle shape and set its radius to 6
        CircleShape circle = new CircleShape();
        circle.setRadius(2.5f);

        spheres.add(body);

        // Create a fixture definition to apply our shape to
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.8f;
        fixtureDef.restitution = 0.6f; // Make it bounce a little bit


        // Create our fixture and attach it to the body
        Fixture fixture = body.createFixture(fixtureDef);

        //Compute shooting vector
        Vector2 shootingVector = new Vector2();
        shootingVector.x = x - cannonPosition.x;
        shootingVector.y = y - cannonPosition.y;
        shootingVector.setLength(4000.f);//400

        body.applyLinearImpulse(shootingVector,cannonPosition,true);

        circle.dispose();
    }

    public void createAsteroid(){
        //Init tools
        PNGtoBox2D converter = new PNGtoBox2D();
        b2Separator splitter = new b2Separator();
        ContourToPolygons triangulator = new ContourToPolygons();

        //Detect contour
        Array<Vector2> v = converter.marchingSquares(new Pixmap(Gdx.files.internal("Asteroids/A1_red.png")));

        // Simplify contour
        Array<Vector2> w = converter.RDP(v, 1.f);
        w.reverse();

        //Create body
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(new Vector2(50.f,50.f));

        // Create a fixture definition to apply our shape to
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.8f;
        fixtureDef.restitution = 0.6f; // Make it bounce a little bit

        // Create our body in the world using our body definition
        Body body = world.createBody(bodyDef);

        // Generate convex hull from simplified contour
        Vector2[] z = w.toArray(Vector2.class);

        Gdx.app.log("Convex Hull check",Integer.toString(splitter.validate(z)));
        //splitter.separate(body,fixtureDef,z);
        triangulator.BuildShape(body,fixtureDef,w);
        Gdx.app.log("Convex Hull","generated");
    }

    private void testarea(){
        //Define a concave shape
        Array<Vector2> v = new Array<Vector2>();
        v.add(new Vector2(200,200));
        v.add(new Vector2(180,170));
        v.add(new Vector2(190,130));
        v.add(new Vector2(220,120));
        v.add(new Vector2(220,90));
        v.add(new Vector2(210,70));
        v.add(new Vector2(230,40));
        v.add(new Vector2(270,90));
        v.add(new Vector2(250,140));
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
        triangulator.BuildShape(body,fixtureDef,v);
    }
}
