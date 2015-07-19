package com.overdrivr.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
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
    private Vector3 v = new Vector3();

    public AnimatedPlant(GameEngine e){
        engine = e;
        modelInstance = new ModelInstance(engine.assetManager.get("Models/B1/B1.g3db", Model.class));

        modelInstance.transform.translate(0, 0, 0);
        modelInstance.transform.rotate(1, 0, 0, 90.f);
        modelInstance.transform.scale(0.05f, 0.05f, 0.05f);

        animator = new AnimationController(modelInstance);
        rootBone = modelInstance.getNode("Bone");

        // Debug display of bones
        ModelBuilder modelBuilder = new ModelBuilder();
        sphere = modelBuilder.createSphere(0.1f, 0.1f, 0.1f, 10, 10,
                new Material(),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        sphereInstance = new ModelInstance(sphere,0,0,0);

        recursiveDebugInfo(rootBone);
    }

    public void render(ModelBatch batch){
        angle += 1f;
        rootBone.getChild(0).rotation.setEulerAngles(angle,0,0);
        modelInstance.calculateTransforms();
        animator.update(Gdx.graphics.getDeltaTime());
        batch.render(modelInstance);

        if(debugDrawBones){
            //rootBone.calculateTransforms(true);
            recursiveRender(batch, rootBone);

            if((int)(angle) == 360 || (int)(angle) == 180){
                Gdx.app.log("AnimatedPlant","Angle ="+angle);
                recursiveDebugInfo(rootBone);
            }
        }
    }

    private void recursiveRender(ModelBatch batch,Node node){
        // Draw current node
        //v = node.calculateWorldTransform().getTranslation(v);
        //sphereInstance.transform.translate(v);
        node.globalTransform.getTranslation(v);
        sphereInstance.transform.setTranslation(v);
        //sphereInstance.transform.set(node.globalTransform);
        batch.render(sphereInstance);
        //If it has children, draw them as well
        for(Node n : node.getChildren()){
            recursiveRender(batch,n);
        }
    }

    private void recursiveDebugInfo(Node node){
        node.globalTransform.getTranslation(v);
        Gdx.app.log("AnimatedPlant", node.id + " : " + v.toString());
        for(Node n : node.getChildren()){
            recursiveDebugInfo(n);
        }
    }
}
