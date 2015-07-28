package com.overdrivr.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Bart on 28/07/2015.
 */
public class AnimatedPlantB1 {
    private final GameEngine engine;
    private ModelInstance modelInstance;
    private AnimationController animator;

    private Node rootBone;

    float angle = 0;

    public boolean debugDrawBones = true;
    private final Model sphere;
    private final ModelInstance sphereInstance;

    private Body groundAnchor;
    private Array<Body> bonesCtrlRight,bonesCtrlLeft;
    private Array<Body> bones;


    public AnimatedPlantB1(GameEngine e){
        engine = e;
        modelInstance = new ModelInstance(engine.assetManager.get("Models/B1/B1.g3db", Model.class));

        modelInstance.transform.translate(0, 0, 0);
        modelInstance.transform.rotate(1, 0, 0, 90.f);
        modelInstance.transform.scale(0.05f, 0.05f, 0.05f);

        animator = new AnimationController(modelInstance);
        rootBone = modelInstance.getNode("Bone");

        bonesCtrlRight = new Array<Body>();
        bonesCtrlLeft = new Array<Body>();
        bones = new Array<Body>();

        // Build Box2D bodies

        // Parametrize the connections
        createStartBone(e.world, new Vector2(0, 0), new Vector2(1, 0));
        //TODO : Modify method to only take final node position
        // and use previous bone position
        addBone(e.world, new Vector2(1, 0), new Vector2(2, 0));


        //Joints
        // Debug display of bones
        ModelBuilder modelBuilder = new ModelBuilder();
        sphere = modelBuilder.createSphere(0.5f, 0.5f, 0.5f, 10, 10,
                new Material(ColorAttribute.createDiffuse(1, 0, 0, 1)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        sphereInstance = new ModelInstance(sphere,0,0,0);
    }

    public void render(ModelBatch batch){
        angle += 1f;
        //rootBone.getChild(0).rotation.setEulerAngles(angle,0,0);
        //rootBone.getChild(0).rotation.setEulerAngles(angle,0,0);
        recursiveUpdate(rootBone.getChild(0),0);

        modelInstance.calculateTransforms();
        animator.update(Gdx.graphics.getDeltaTime());
        batch.render(modelInstance);

        // Update anchor
        //setCtrlPosition((float) (Math.sin(angle / 100.f)), 2);

        if(debugDrawBones){
            recursiveRender(batch, rootBone);
        }
    }

    // Applies rotation of b to node n
    private void recursiveUpdate(Node n,int startIndex){
        if(startIndex >= bones.size)
            return;

        //if(startIndex>=1)
        //    return;

        n.rotation.set(Vector3.Y,(float) Math.toDegrees(bones.get(startIndex).getAngle()));

        startIndex++;

        if(n.hasChildren())
            recursiveUpdate(n.getChild(0),startIndex);
    }

    public void setCtrlPosition(float x, float y){
        // TODO : Should move handles while keeping their individual offsets
        for(Body b : bonesCtrlLeft)
            b.setTransform(x + 1, y, 0);
        for(Body b : bonesCtrlRight)
            b.setTransform(x - 1, y, 0);
    }

    private void recursiveRender(ModelBatch batch,Node node){
        Matrix4 m = modelInstance.transform.cpy();
        m.mul(node.globalTransform);
        sphereInstance.transform.set(m);
        batch.render(sphereInstance);
        //If it has children, draw them as well
        for(Node n : node.getChildren()){
            recursiveRender(batch, n);
        }
    }

    private void createStartBone(World w, Vector2 startPosition, Vector2 endPosition){
        /// Ground anchor
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(startPosition);
        groundAnchor = w.createBody(bodyDef);

        /// First bone
        BodyDef bodyDef2 = new BodyDef();
        bodyDef2.type = BodyDef.BodyType.DynamicBody;
        bodyDef2.position.set(endPosition);
        bodyDef2.angle = 0;
        bones.add(w.createBody(bodyDef2));

        MyBodyData data = new MyBodyData();
        data.type = BodyType.BODY_TYPE_DEFAULTS;
        bones.first().setUserData(data);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.1f, 1f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 5.0f;
        fixtureDef.restitution = 0.6f;
        bones.first().createFixture(fixtureDef);

        /// Resting anchor
        BodyDef bodyDef3 = new BodyDef();
        bodyDef3.type = BodyDef.BodyType.KinematicBody;
        bodyDef3.position.set(1, 2);
        bonesCtrlRight.add(w.createBody(bodyDef3));

        BodyDef bodyDef4 = new BodyDef();
        bodyDef4.type = BodyDef.BodyType.KinematicBody;
        bodyDef4.position.set(-1,2);
        bonesCtrlLeft.add(w.createBody(bodyDef4));

        /// Joint to ground anchor
        RevoluteJointDef jointDef = new RevoluteJointDef();
        jointDef.bodyA = groundAnchor;
        jointDef.bodyB = bones.first();
        //jointDef.frequencyHz=10.f;
        //jointDef.dampingRatio = 0.1f;
        jointDef.localAnchorB.set(0,-1f);
        engine.world.createJoint(jointDef);

        /// Joint to resting anchor
        DistanceJointDef jointDef2 = new DistanceJointDef();
        jointDef2.bodyA = bonesCtrlRight.first();
        jointDef2.bodyB = bones.first();
        jointDef2.frequencyHz=0.3f;
        jointDef2.dampingRatio = 0.1f;
        engine.world.createJoint(jointDef2);

        DistanceJointDef jointDef3 = new DistanceJointDef();
        jointDef3.bodyA = bonesCtrlLeft.first();
        jointDef3.bodyB = bones.first();
        jointDef3.frequencyHz=0.3f;
        jointDef3.dampingRatio = 0.1f;
        engine.world.createJoint(jointDef3);

        shape.dispose();
    }

    private void addBone(World w, Vector2 startPosition, Vector2 endPosition){
        /// bone n + 1
        BodyDef bodyDef2 = new BodyDef();
        bodyDef2.type = BodyDef.BodyType.DynamicBody;
        bodyDef2.position.set(endPosition);
        bodyDef2.angle = 0;
        bones.add(w.createBody(bodyDef2));

        MyBodyData data = new MyBodyData();
        data.type = BodyType.BODY_TYPE_DEFAULTS;
        bones.get(bones.size-1).setUserData(data);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.1f, 1f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 5.0f;
        fixtureDef.restitution = 0.6f;
        bones.get(bones.size - 1).createFixture(fixtureDef);

        /// Resting anchor
        BodyDef bodyDef3 = new BodyDef();
        bodyDef3.type = BodyDef.BodyType.KinematicBody;
        bodyDef3.position.set(endPosition.x, endPosition.y+5);
        bonesCtrlRight.add(w.createBody(bodyDef3));

        BodyDef bodyDef4 = new BodyDef();
        bodyDef4.type = BodyDef.BodyType.KinematicBody;
        bodyDef4.position.set(-1,2);
        bonesCtrlLeft.add(w.createBody(bodyDef4));

        /// Joint to previous bone
        RevoluteJointDef jointDef = new RevoluteJointDef();
        jointDef.bodyA = bones.get(bones.size-1);
        jointDef.bodyB = bones.get(bones.size-2);
        //jointDef.frequencyHz=10.f;
        //jointDef.dampingRatio = 0.1f;
        jointDef.localAnchorA.set(0,-1f);
        jointDef.localAnchorB.set(0,1f);
        jointDef.collideConnected = false;
        engine.world.createJoint(jointDef);

        /// Joint to resting anchor
        DistanceJointDef jointDef2 = new DistanceJointDef();
        jointDef2.bodyA = bonesCtrlRight.get(bones.size - 1);
        jointDef2.bodyB = bones.get(bones.size - 1);
        jointDef2.frequencyHz=0.3f;
        jointDef2.dampingRatio = 0.1f;
        engine.world.createJoint(jointDef2);

        DistanceJointDef jointDef3 = new DistanceJointDef();
        jointDef3.bodyA = bonesCtrlLeft.get(bones.size - 1);
        jointDef3.bodyB = bones.get(bones.size-1);
        jointDef3.frequencyHz=0.3f;
        jointDef3.dampingRatio = 0.1f;
        engine.world.createJoint(jointDef3);

        shape.dispose();
    }
}
