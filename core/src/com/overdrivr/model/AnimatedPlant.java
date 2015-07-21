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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.JointDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.utils.Array;
import com.sun.org.apache.xpath.internal.operations.Mod;

/**
 * Created by Bart on 17/07/2015.
 */
public class AnimatedPlant {
    private final GameEngine engine;
    private ModelInstance modelInstance;
    private AnimationController animator;

    private Node rootBone;

    float angle = 0;

    public boolean debugDrawBones = true;
    private final Model sphere;
    private final ModelInstance sphereInstance;

    public Body groundAnchor;
    private Body restingAnchor;
    public Body body;

    public AnimatedPlant(GameEngine e){
        engine = e;
        modelInstance = new ModelInstance(engine.assetManager.get("Models/B1/B1.g3db", Model.class));

        modelInstance.transform.translate(0, 0, 0);
        modelInstance.transform.rotate(1, 0, 0, 90.f);
        modelInstance.transform.scale(0.05f, 0.05f, 0.05f);

        animator = new AnimationController(modelInstance);
        rootBone = modelInstance.getNode("Bone");

        // Build Box2D bodies
        /// Ground anchor
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(0, 0);
        groundAnchor = e.world.createBody(bodyDef);

        /// First bone
        BodyDef bodyDef2 = new BodyDef();
        bodyDef2.type = BodyDef.BodyType.DynamicBody;
        bodyDef2.position.set(0,5);
        bodyDef2.angle = 0;
        body = e.world.createBody(bodyDef2);

        /// Resting anchor
        BodyDef bodyDef3 = new BodyDef();
        bodyDef3.type = BodyDef.BodyType.KinematicBody;
        bodyDef3.position.set(-1,3);
        restingAnchor = e.world.createBody(bodyDef3);

        MyBodyData data = new MyBodyData();
        data.type = BodyType.BODY_TYPE_DEFAULTS;
        body.setUserData(data);

        CircleShape shape = new CircleShape();
        shape.setRadius(0.1f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 5.0f;
        fixtureDef.restitution = 0.6f;
        body.createFixture(fixtureDef);

        //Joints
        /// Joint to ground anchor
        DistanceJointDef jointDef = new DistanceJointDef();
        jointDef.bodyA = groundAnchor;
        jointDef.bodyB = body;
        jointDef.frequencyHz=10.f;
        jointDef.dampingRatio = 0.1f;
        engine.world.createJoint(jointDef);

        /// Joint to resting anchor
        /*DistanceJointDef jointDef2 = new DistanceJointDef();
        jointDef2.bodyA = restingAnchor;
        jointDef2.bodyB = body;
        jointDef2.frequencyHz=10.f;
        jointDef2.dampingRatio = 0.1f;
        engine.world.createJoint(jointDef2);*/

        shape.dispose();

        // Debug display of bones
        ModelBuilder modelBuilder = new ModelBuilder();
        sphere = modelBuilder.createSphere(0.5f, 0.5f, 0.5f, 10, 10,
                new Material(ColorAttribute.createDiffuse(1, 0, 0, 1)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        sphereInstance = new ModelInstance(sphere,0,0,0);
    }

    public void render(ModelBatch batch){
        angle += 1f;
        rootBone.getChild(0).rotation.setEulerAngles(angle,0,0);
        modelInstance.calculateTransforms();
        animator.update(Gdx.graphics.getDeltaTime());
        batch.render(modelInstance);

        // Update anchor
        //restingAnchor.setLinearVelocity((float)(Math.sin(angle/30.f)),0);
        body.applyForceToCenter((float)(Math.sin(angle/30.f)),0,true);

        if(debugDrawBones){
            //rootBone.calculateTransforms(true);
            recursiveRender(batch, rootBone);
        }
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
}
