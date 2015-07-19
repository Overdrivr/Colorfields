package com.overdrivr.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.utils.BaseJsonReader;

/**
 * Created by Bart on 05/07/2015.
 */
public class Assets {

    public void loadAll(AssetManager manager){
        manager.load("Models/B1/B1.g3db",Model.class);
    }
}
