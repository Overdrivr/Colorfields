package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by Bart on 17/05/2015.
 */
public class GravityField {
    Vector2 [][] forces;
    float unitsize;
    Vector2 gridcenter;
    int cellamount;

    // @center : center of the grid
    // @amount : amount of grid cells per side
    // @cellsize : size of the side of a cell
    public GravityField(Vector2 center, int amount, float cellsize){
        forces = new Vector2[amount][amount];
        unitsize = cellsize;
        gridcenter = center;
        cellamount = amount;
    }

    public Vector2 getForce(float x, float y, Color color){
        int grid_x = (int)((x - gridcenter.x) / unitsize);
        int grid_y = (int)((y - gridcenter.y) / unitsize);

        // Outside the grid, no force is applied
        if (grid_x < 0 || grid_x > cellamount ||
            grid_y < 0 || grid_y > cellamount) {
            return new Vector2(0,0);
        }

        // Utiliser couleur de l'objet et couleur actuelle pour obtenir force finale
        // todo

        // Return final force value
        return forces[grid_x][grid_y];
    }

    public void debug_draw(ShapeRenderer renderer, Matrix4 combined){

        renderer.setColor(1, 1, 0, 1);
        for(int i = 0 ; i < cellamount ; i++) {
            float x1 = i * unitsize + gridcenter.x;
            float x2 = i * unitsize + gridcenter.x;
            float y1 = 0 * unitsize + gridcenter.y;
            float y2 = (cellamount - 1) * unitsize + gridcenter.y;
            renderer.line(x1,y1,x2,y2);
            x1 = 0 * unitsize + gridcenter.x;
            x2 = (cellamount - 1) * unitsize + gridcenter.x;
            y1 = i * unitsize + gridcenter.y;
            y2 = i * unitsize + gridcenter.y;
            renderer.line(x1,y1,x2,y2);
        }
    }
}
