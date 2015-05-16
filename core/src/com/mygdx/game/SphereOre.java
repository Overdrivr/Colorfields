package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;

import java.awt.Polygon;

/**
 * Created by Bart on 16/05/2015.
 */
public class SphereOre extends Actor{
    private final GameEngine g;
    TextureRegion texture;
    Image redcross;
    Image purplecross;
    Image asset;
    Sprite sprite;
    public Body body;
    Group root;

    public SphereOre(final GameEngine game, String filename, Vector2 position){
        g = game;

        //Get pixmap
        g.assetManager.load(filename, Pixmap.class);

        //Gdx.app.log("MassiveAsteroids", "Loading asset");
        do{
            //Gdx.app.log("MassiveAsteroids", Float.toString(g.assetManager.getProgress()));
        }while(!g.assetManager.update());

        if(g.assetManager.isLoaded(filename)){
            //Gdx.app.log("MassiveAsteroids","Loaded pixmap");
            // Get the pixmap
            Pixmap p = g.assetManager.get(filename,Pixmap.class);

            // Create the sprite
            texture = new TextureRegion(new Texture(p));
            sprite = new Sprite(texture);


            //Create body
            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.DynamicBody;
            bodyDef.position.set(position);


            /*CircleShape shape = new CircleShape();
            shape.setRadius(50.f);*/



            // Create a fixture definition to apply our shape to
            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.density = 0.5f;
            fixtureDef.friction = 0.8f;
            fixtureDef.restitution = 0.6f; // Make it bounce a little bit

            // Create our body in the world using our body definition
            body = g.world.createBody(bodyDef);

            PolygonShape shape = new PolygonShape();
            Vector2[] vec = new Vector2[4];
            vec[0] = new Vector2(0,0);
            vec[1] = new Vector2(30,0);
            vec[2] = new Vector2(30,30);
            vec[3] = new Vector2(0,30);
            shape.set(vec);
            fixtureDef.shape = shape;
            body.createFixture(fixtureDef);

            vec[0] = new Vector2(30,0);
            vec[1] = new Vector2(50,0);
            vec[2] = new Vector2(50,20);
            vec[3] = new Vector2(30,20);
            shape.set(vec);
            fixtureDef.shape = shape;
            body.createFixture(fixtureDef);


            g.stage.addActor(this);



            purplecross = new Image(new TextureRegion(new Texture(Gdx.files.internal("TestAssets/purplecross.png"))));
            g.stage.addActor(purplecross);

            //Intermediate node that will handle rotation around physical body
            root = new Group();
            root.setTransform(true);
            g.stage.addActor(root);

            //Center of group
            redcross = new Image(new TextureRegion(new Texture(Gdx.files.internal("TestAssets/redcross.png"))));
            root.addActor(redcross);

            //Asset attached to group
            Texture t1 = new Texture(Gdx.files.internal(filename));
            TextureRegion t = new TextureRegion(t1);
            t.flip(false,true);
            asset = new Image(t);
            //asset.setPosition(0,0,0);
            //Gdx.app.log("Asset size", Float.toString() + " " + Float.toString());
            root.addActor(asset);
        }
    }

    public void draw(Batch batch, float parentAlpha){
        //root.setPosition(body.getWorldCenter().x, body.getWorldCenter().y);
        root.setPosition(body.getPosition().x,body.getPosition().y);
        root.setRotation((float) (Math.toDegrees(body.getAngle())));
        root.draw(batch, parentAlpha);
        purplecross.setPosition(body.getPosition().x, body.getPosition().y);
        redcross.setPosition(asset.getX(), asset.getY());
    }
}
